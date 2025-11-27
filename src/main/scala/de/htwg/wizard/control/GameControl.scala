package de.htwg.wizard.control
import de.htwg.wizard.model.*
import de.htwg.wizard.view.*

class GameControl (view: GameView) {
  
  
  def runGame(): Unit =
    var state = initGame()

    state = prepareNextRound(state.copy(currentRound = 0))
    state.notifyObservers()

    for (i <- 1 to state.totalRounds) do
      state = playRound(state)
      state.notifyObservers()
      state = prepareNextRound(state)
      state.notifyObservers()

    finishGame(state)
    state.notifyObservers()

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
      state.notifyObservers()
      state

    catch
      case _: NumberFormatException =>
        view.showError("Invalid entry! Try again.")
        initGame()

  private def finishGame(gameState: GameState): Unit =
    val winner = gameState.players.maxBy(_.totalPoints)
    view.showGameWinner(winner)

  private def prepareNextRound(gameState: GameState): GameState =
    if gameState.currentRound >= gameState.totalRounds then gameState
    else
      val newRound = gameState.currentRound + 1

      var deck = Deck().shuffle()

      val players = gameState.players.map { p =>
        val (hand, restDeck) = deck.deal(newRound)
        deck = restDeck
        p.copy(hand = hand)
      }

      gameState.copy(
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
    val afterPrediction = gameState.copy(players = updatedPlayers)
    afterPrediction.notifyObservers()
    var state = afterPrediction

    val tricksThisRound = state.players.head.hand.size

    for (round <- 1 to tricksThisRound) do
      state = playOneTrick(state)
      state.notifyObservers()

    val updated = state.players.map { player =>
      player.copy(
        totalPoints = player.totalPoints + calculateRoundPoints(player)
      )
    }
    state = state.copy(players = updated)
    state.notifyObservers()

    view.showRoundEvaluation(state.currentRound, updated)

    val updated2 = state.players.map { player =>
      player.copy(
        tricks = 0,
        predictedTricks = 0
      )
    }
    state = state.copy(players = updated2)
    state.notifyObservers()

    state

  private def calculateRoundPoints(p: Player): Int =
    if p.predictedTricks == p.tricks then 20 + p.tricks * 10
    else (p.tricks * 10) + (-10 * (p.tricks - p.predictedTricks).abs)


  private def playOneTrick(gameState: GameState): GameState =
    // Fehlerfall: irgendein Spieler hat keine Karten → kein aktiver Stich
    if gameState.players.exists(_.hand.isEmpty) then
      view.showError("No active stitch!")
      return gameState

    val handSize = gameState.players.head.hand.size
    view.showTrickStart(handSize)

    // Spieler spielen Karten
    val (updatedPlayers, playedFrom) =
      gameState.players.map { player =>
        view.askPlayerCard(player)
        val index = view.readIndex(player)
        val playedCard = player.hand(index)
        val newHand = player.hand.patch(index, Nil, 1)
        (player.copy(hand = newHand), (player.id, playedCard))
      }.unzip

    val newTrick = Trick(playedFrom.toMap)

    val state = gameState.copy(
      players = updatedPlayers,
      currentTrick = Some(newTrick)
    )
    state.notifyObservers()

    val (winnerId, winningCard) = whoWonStitch(newTrick, state.currentTrump)

    val updatedPlayers2 = state.players.map { player =>
      if player.id == winnerId then player.copy(tricks = player.tricks + 1)
      else player
    }

    val winner = updatedPlayers2.find(_.id == winnerId).get
    view.showTrickWinner(winner, winningCard)

    state.copy(players = updatedPlayers2, currentTrick = None)


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
