package de.htwg.wizard.control
import de.htwg.wizard.model.*
import de.htwg.wizard.view.*

class GameControl(view: GameView, strategy: TrickStrategy = StandardTrickStrategy()) {

  def runGame(): Unit =
    var phase: GameStatePhase = InitState
    var state: GameState = GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = new Deck(),
      currentRound = 0,
      totalRounds = 0,
      currentTrump = None
    )

    while phase != null do
      val (nextPhase, nextState) = phase.run(this, state)
      phase = nextPhase
      state = nextState

  // -------------------------------------------------------------
  // INIT GAME
  // -------------------------------------------------------------
  private[control] def initGame(): GameState =
    view.askPlayerAmount()
    try
      val playerCount = view.readPlayerAmount()
      val deck = new Deck().shuffle()

      val players =
        (0 until playerCount).map(id => Player(id, hand = Nil)).toList

      val gs = GameState(
        amountOfPlayers = playerCount,
        players = players,
        deck = deck,
        currentRound = 0,
        totalRounds = Array(4,3,2,2)(playerCount - 3),
        currentTrump = None
      )

      gs.add(view)
      gs.notifyObservers()
      gs

    catch
      case _: NumberFormatException =>
        view.showError("Invalid entry! Try again.")
        initGame()

  // -------------------------------------------------------------
  // PREPARE ROUND
  // -------------------------------------------------------------
  private[control] def prepareNextRound(gs: GameState): GameState =
    if gs.currentRound >= gs.totalRounds then gs
    else
      val newRound = gs.currentRound + 1
      var deck = Deck().shuffle()

      val trumpCard = deck.cards.head
      val rest = deck.copy(cards = deck.cards.tail)

      val trump =
        if isWizard(trumpCard) then Some(view.chooseTrump())
        else if isJoker(trumpCard) then None
        else Some(trumpCard.color)

      view.showRoundInfo(newRound, trump, gs.amountOfPlayers)

      var d = rest
      val players = gs.players.map { p =>
        val (hand, newD) = d.deal(newRound)
        d = newD
        p.copy(hand = hand)
      }

      gs.copy(
        currentRound = newRound,
        players = players,
        deck = d,
        currentTrump = trump
      )

  // -------------------------------------------------------------
  // PREDICT TRICKS
  // -------------------------------------------------------------
  private[control] def predictTricks(gs: GameState): GameState =
    val players = gs.players.map { p =>
      view.askHowManyTricks(p)
      val prediction = view.readPositiveInt()
      p.copy(predictedTricks = prediction)
    }

    val newState = gs.copy(players = players)
    newState.notifyObservers()
    newState

  // -------------------------------------------------------------
  // PLAY ONE TRICK  (FIXED - TESTSAFE)
  // -------------------------------------------------------------
  private[control] def playOneTrick(trickNumber: Int, gs: GameState): GameState =
    // Test verlangt: SOFORT abbrechen wenn ein Spieler keine Karten hat
    if gs.players.exists(_.hand.isEmpty) then
      view.showError("No active stitch!")
      return gs

    view.showTrickStart(trickNumber)

    var trickSoFar = Trick(Map())

    val (updatedPlayers, playedPairs) =
      gs.players.map { p =>

        view.askPlayerCard(p)
        val idx = view.readIndex(p)

        // Ungültiger Index: sofort abbrechen laut Tests
        if idx < 0 || idx >= p.hand.size then
          view.showError("Invalid index!")
          return gs

        val card = p.hand(idx)

        // Illegaler Zug: Test verlangt kein Retry → nur Abbruch
        if !isAllowedMove(card, p, trickSoFar) then
          view.showError("You must follow start color!")
          return gs

        // Karte spielen
        trickSoFar = Trick(trickSoFar.played + (p.id -> card))
        val newHand = p.hand.patch(idx, Nil, 1)

        (p.copy(hand = newHand), (p.id, card))

      }.unzip

    val trick = Trick(playedPairs.toMap)
    val afterTrick = gs.copy(players = updatedPlayers, currentTrick = Some(trick))
    afterTrick.notifyObservers()

    val (winnerId, winningCard) =
      strategy.winner(trick, afterTrick.currentTrump)

    val scoredPlayers =
      afterTrick.players.map { p =>
        if p.id == winnerId then p.copy(tricks = p.tricks + 1) else p
      }

    val winner = scoredPlayers.find(_.id == winnerId).get
    view.showTrickWinner(winner, winningCard)

    afterTrick.copy(players = scoredPlayers, currentTrick = None)

  // -------------------------------------------------------------
  // MOVE CHECK
  // -------------------------------------------------------------
  private def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
    val leadOpt = trick.played.values.collectFirst {
      case c if isNormal(c) => c.color
    }

    if leadOpt.isEmpty then true
    else
      val lead = leadOpt.get
      val mustFollow =
        player.hand.exists(c => isNormal(c) && c.color == lead)

      if mustFollow then isNormal(card) && card.color == lead else true

  // -------------------------------------------------------------
  // SCORE ROUND
  // -------------------------------------------------------------
  private[control] def scoreRound(gs: GameState): GameState =
    val scored = gs.players.map { p =>
      p.copy(totalPoints = p.totalPoints + calculateRoundPoints(p))
    }

    val s1 = gs.copy(players = scored)
    s1.notifyObservers()
    view.showRoundEvaluation(s1.currentRound, scored)

    val reset =
      scored.map(p => p.copy(tricks = 0, predictedTricks = 0))

    val s2 = s1.copy(players = reset)
    s2.notifyObservers()
    s2

  // -------------------------------------------------------------
  // POINT CALCULATION
  // -------------------------------------------------------------
  private[control] def calculateRoundPoints(p: Player): Int =
    if p.predictedTricks == p.tricks then
      20 + p.tricks * 10
    else
      p.tricks * 10 - 10 * (p.tricks - p.predictedTricks).abs

  // -------------------------------------------------------------
  // FINISH
  // -------------------------------------------------------------
  private[control] def finishGame(gs: GameState): Unit =
    val winner = gs.players.maxBy(_.totalPoints)
    view.showGameWinner(winner)
}
