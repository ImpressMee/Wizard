package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.*

import scala.io.StdIn.readLine
class GameControl (view: GameView) {

  def runGame(): Unit =
    var state = initGame()

    // Runde 1 vorbereiten (1 Karte austeilen)
    state = prepareNextRound(state.copy(currentRound = 0))

    for (i <- 1 to state.totalRounds) do

      state = playRound(state)
      state = prepareNextRound(state)

    finishGame(state)

  private def initGame(): GameState =
    view.askPlayerAmount()
    try
      val playerCount = view.readPlayerAmount()
      val deck = new Deck()
      deck.shuffle()

      // All players are starting with empty hands
      val players = (0 until playerCount)
        .map(id => Player(id, hand = List()))
        .toList

      val trump = trumpColor()

      val state = GameState(
        amountOfPlayers = playerCount,
        players = players,
        deck = deck,
        currentRound = 0,
        totalRounds = Array(4, 3, 2, 2)(playerCount - 3),
        currentTrump = trump
      )
      state.add(view)
      state

    catch
      case _: NumberFormatException =>
        view.showError("Invalid entry! Try again.")
        initGame()

  private def finishGame(gameState: GameState): Unit =
    val winner = gameState.players.maxBy(_.totalPoints)
    view.showGameWinner(winner)

  private def prepareNextRound(gs: GameState): GameState =
    if gs.currentRound >= gs.totalRounds then gs
    else
      val newRound = gs.currentRound + 1

      var deck = Deck().shuffle()
      
      val players = gs.players.map { p =>
        val (hand, restDeck) = deck.deal(newRound)
        deck = restDeck
        p.copy(hand = hand)
      }

      gs.copy(
        currentRound = newRound,
        players = players,
        deck = deck,
        currentTrump = trumpColor()
      )


  private def playRound(gameState: GameState): GameState =
    view.showRoundInfo(gameState.currentRound, gameState.currentTrump, gameState.amountOfPlayers)

    val updatedPlayers = gameState.players.map { player =>
      view.askHowManyTricks(player)
      val prediction = view.readPositiveInt()
      player.copy(predictedTricks = prediction)
    }
    gameState.copy(players = updatedPlayers)

    var state = gameState
    val tricksThisRound = math.min(state.currentRound, 3) // Für Testzwecke reduziert

    for (round <- 1 to tricksThisRound) do
      state = playOneTrick(state)

    val updated = state.players.map { p =>
      p.copy(
        totalPoints = p.totalPoints + calculateRoundPoints(p)
      )
    }
    state.copy(players = updated)

    view.showRoundEvaluation(state.currentRound, updated)

    val updated2 = state.players.map { p =>
      p.copy(
        totalPoints = p.totalPoints + calculateRoundPoints(p),
        tricks = 0,
        predictedTricks = 0
      )
    }
    state.copy(players = updated2)

  private def calculateRoundPoints(p: Player): Int =
    if p.predictedTricks == p.tricks then 20 + p.tricks * 10
    else (p.tricks * 10) + (-10 * (p.tricks - p.predictedTricks).abs)


  private def playOneTrick(gameState: GameState): GameState =
    val handSize = gameState.players.head.hand.size
    view.showTrickStart(handSize)
    // map with updated players
    // and playedFrom map that contains player.id and playedCard
    val (updatedPlayers, playedFrom) =
      gameState.players.map { player =>

        view.askPlayerCard(player)
        val index = view.readIndex(player)
        val playedCard = player.hand(index)
        val newHand = player.hand.patch(index, Nil, 1)

        (player.copy(hand = newHand), (player.id, playedCard))
      }.unzip

    val newTrick = Trick(played = playedFrom.toMap)

    gameState.copy(
      players = updatedPlayers,
      currentTrick = Some(newTrick)
    )
    //finish Trick
    gameState.currentTrick match
      case None =>
        view.showError("No active stitch!")
        gameState

      case Some(trick) =>
        val (winnerId, winningCard) = whoWonStitch(trick, gameState.currentTrump)

        val updatedPlayers = gameState.players.map { player =>
          if player.id == winnerId then player.copy(tricks = player.tricks + 1)
          else player
        }

        val winner = updatedPlayers.find(_.id == winnerId).get
        view.showTrickWinner(winner, winningCard)

        gameState.copy(players = updatedPlayers, currentTrick = None)

  private def whoWonStitch(stitch: Trick, trump: CardColor): (Int, Card) =
    var bestId = -1
    var bestCard: Card = null

    for ((pid, card) <- stitch.played) do

      if bestCard == null then
        bestId = pid;
        bestCard = card

      else if card.color == trump then
        if bestCard.color != trump || card.value > bestCard.value then
          bestId = pid;
          bestCard = card

      else if bestCard.color != trump && card.value > bestCard.value then
        bestId = pid;
        bestCard = card

    (bestId, bestCard)


}
