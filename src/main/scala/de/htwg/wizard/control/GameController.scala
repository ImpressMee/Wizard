package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.*

import scala.io.StdIn.readLine

class GameController(view: GameView) {

  def initGame(input: => String = readLine()): GameState =
    view.askPlayerAmount()
    try
      val playerCount = input.toInt
      if playerCount < 3 || playerCount > 6 then
        view.showError("Wrong amount! Try again.")
        initGame(input)
      else
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
          currentRound = 1,
          totalRounds = Array(4, 3, 2, 2)(playerCount - 3),
          currentTrump = trump
        )
        state.add(view)
        state

    catch
      case _: NumberFormatException =>
        view.showError("Invalid entry! Try again.")
        initGame(input)

  def startRound(gs: GameState, input: => String = readLine()): GameState =
    view.showRoundInfo(gs.currentRound, gs.currentTrump, gs.amountOfPlayers)
    
    val updatedPlayers = gs.players.map { p =>
      view.showPlayerCards(p)
      view.askHowManyTricks(p)
      val prediction = readPositiveInt(readLine(), view)
      p.copy(predictedTricks = prediction)
    }
    gs.copy(players = updatedPlayers)

  def readPositiveInt(input: => String, view: GameView): Int =
    for attempt <- Iterator.continually(input) do
      try
        val value = attempt.toInt
        if value >= 0 then return value
        else view.showError("Value must be 0 or greater!")
      catch case _: NumberFormatException =>
        view.showError("Please enter a valid number!")
    -1

  def playOneStitch(gs: GameState, input: => String = readLine()): GameState =
    val size = gs.players.head.hand.size
    view.showTrickStart(size)
    val (updatedPlayers, playedPairs) =
      (for p <- gs.players yield
        view.askPlayerCard(p)
        val index = readValidIndex(p, input, view) //  Aufruf
        val playedCard = p.hand(index)
        // patch(index, replacement, remove)
        val newHand = p.hand.patch(index, Nil, 1)
        (p.copy(hand = newHand), p.id -> playedCard)
        ).unzip

    val newStitch = Trick(played = playedPairs.toMap)

    gs.copy(
      players = updatedPlayers,
      currentTrick = Some(newStitch)
    )

  def readValidIndex(p: Player, input: => String, view: GameView): Int =
    for attempt <- Iterator.continually(input) do
      try
        val index = attempt.toInt
        if index >= 0 && index < p.hand.length then return index
        else view.showError("Index out of range! Try again.")
      catch case _: NumberFormatException =>
        view.showError("Please enter a valid number!")
    -1 // shouldnt be reached

  def whoWonStitch(stitch: Trick, trump: CardColor): (Int, Card) =
    val played = stitch.played

    var bestId = -1
    var bestCard: Card = null

    for ((pid, card) <- played) do

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

  def finishStitch(gs: GameState): GameState =
    gs.currentTrick match
      case None =>
        view.showError("No active stitch!")
        gs

      case Some(stitch) =>
        val (winnerId, winningCard) = whoWonStitch(stitch, gs.currentTrump)

        val updatedPlayers = gs.players.map { p =>
          if p.id == winnerId then p.copy(tricks = p.tricks + 1)
          else p
        }

        val winner = updatedPlayers.find(_.id == winnerId).get
        view.showTrickWinner(winner, winningCard)

        gs.copy(players = updatedPlayers, currentTrick = None)

  def playFullRound(gs: GameState): GameState =
    var state = gs

    val stitchesThisRound = math.min(state.currentRound, 3) // Für Testzwecke reduziert

    for (round <- 1 to stitchesThisRound) do
      state = playOneStitch(state)
      state = finishStitch(state)

    state

  def calculateRoundPoints(p: Player): Int =
    if p.predictedTricks == p.tricks then 20 + p.tricks * 10
    else -10 * (p.tricks - p.predictedTricks).abs

  def finishRound(gs: GameState): GameState =
    val updated = gs.players.map { p =>
      p.copy(
        totalPoints = p.totalPoints + calculateRoundPoints(p)
      )
    }
    gs.copy(players = updated)

    view.showRoundEvaluation(gs.currentRound, updated)

    val updated2 = gs.players.map { p =>
      p.copy(
        totalPoints = p.totalPoints + calculateRoundPoints(p),
        tricks = 0,
        predictedTricks = 0
      )
    }
    gs.copy(players = updated2)

  def prepareNextRound(gs: GameState): GameState =
    if gs.currentRound >= gs.totalRounds then gs
    else
      val newRound = gs.currentRound + 1

      val deck = new Deck()
      deck.shuffle()

      // for each player in map
      val players = gs.players.map { p =>
        val (hand, restDeck) = deck.deal(newRound)
        p.copy(hand = hand)
      }

      gs.copy(
        currentRound = newRound,
        players = players,
        deck = deck,
        currentTrump = trumpColor()
      )

  def finishGame(gs: GameState): Unit =
    val winner = gs.players.maxBy(_.totalPoints)
    view.showGameWinner(winner)

  def runGame(): Unit =
    var state = initGame()

    // Runde 1 vorbereiten (1 Karte austeilen)
    state = prepareNextRound(state.copy(currentRound = 0))

    for (i <- 1 to state.totalRounds) do

      state = startRound(state)
      state = playFullRound(state)
      state = finishRound(state)
      state = prepareNextRound(state)

    finishGame(state)

}
