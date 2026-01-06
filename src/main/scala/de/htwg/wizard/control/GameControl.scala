package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import de.htwg.wizard.control.strategy.{StandardTrickStrategy, TrickStrategy}

import scala.util.{Failure, Success, Try}
import scala.util.boundary
import scala.util.boundary.break
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.observer.Observer

class GameControl(
                   val strategy: TrickStrategy = StandardTrickStrategy()
                 ) {

  // ============================================================
  // MEMENTO (undo management)
  // ============================================================

  private var history: List[GameStateMemento] = Nil

  private[control] def saveState(gs: GameState): Unit =
    history = gs.createMemento() :: history

  def undo(current: GameState): GameState =
    history match
      case m :: rest =>
        history = rest
        current.restore(m)
      case Nil =>
        current

  // ============================================================
  // GUI / TUI ENTRY POINTS
  // ============================================================

  var currentState: GameState = _
  private var currentPhase: Option[GameStatePhase] = _
  
  def submitPlayerAmount(n: Int): Unit =
    saveState(currentState)
    currentState = doInitGame(n)
    currentPhase = Some(PrepareRoundState)
    step()


  def prepareNextRound(trump: Option[CardColor]): Unit =
    saveState(currentState)
    currentState = doPrepareNextRound(currentState, trump)

    if currentState.currentRound >= currentState.totalRounds then
      currentPhase = Some(FinishState)
    else
      currentPhase = Some(PredictState)

    step()

  def submitPredictions(predictions: Map[Int, Int]): Unit =
    saveState(currentState)
    currentState = doPredictTricks(currentState, predictions)
    currentPhase = Some(TrickState(1))
    step()

  def playTrick(trickNr: Int, moves: Map[Int, Int]): Unit =
    saveState(currentState)

    val before = currentState.players.head.hand.size
    val next = doPlayOneTrick(trickNr, currentState, moves)
    val after = next.players.head.hand.size

    currentState = next

    if after == before then
      currentPhase = Some(TrickState(trickNr)) // ungültiger Stich
    else if after == 0 then
      currentPhase = Some(ScoreState) // Runde fertig
    else
      currentPhase = Some(TrickState(trickNr + 1)) // nächster Stich

    step()


  // ============================================================
  // GAME LOOP
  // ============================================================

  def runGame(view: Observer): Unit =
    currentState = GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = new Deck(),
      currentRound = 0,
      totalRounds = 0,
      currentTrump = None
    )

    currentPhase = Some(InitState)

    // Observer anmelden
    currentState.add(view)

    // erste Phase auslösen
    step()

  private def step(): Unit =
    currentPhase match
      case Some(phase) =>
        val (nextPhase, nextState) = phase.run(this, currentState)
        currentState = nextState
        currentPhase = nextPhase
      case None =>


  // ============================================================
  // INITIALIZATION
  // ============================================================

  private[control] def doInitGame(playerCount: Int): GameState =

      val deck = new Deck().shuffle()
      val players = (0 until playerCount).map(id => Player(id)).toList

      val gs = GameState(
        amountOfPlayers = playerCount,
        players = players,
        deck = deck,
        currentRound = 0,
        totalRounds = Array(4, 3, 2, 2)(playerCount - 3),
        currentTrump = None
      )

      gs.notifyObservers(StateChanged(gs))
      gs

  // ============================================================
  // PREPARE NEXT ROUND
  // ============================================================

  private[control] def doPrepareNextRound(gs: GameState, trump: Option[CardColor]): GameState = {
      saveState(gs)
      val newRound = gs.currentRound + 1

      // create a fresh full deck every round
      val fullDeck = Deck().shuffle()
      val deckAfterTrump = fullDeck.copy(cards = fullDeck.cards.tail)

      val playersWithHands =
        gs.players.foldLeft((List.empty[Player], deckAfterTrump)) {
          case ((acc, deck), p) =>
            val (hand, nextDeck) = deck.deal(newRound)
            (acc :+ p.copy(hand = hand), nextDeck)
        } match
          case (players, deck) => (players, deck)


      val newState = gs.copy(
          currentRound = newRound,
          players = playersWithHands._1,
          deck = playersWithHands._2,
          currentTrump = trump
        )
        newState.notifyObservers(RoundStarted(newRound, newState))
        newState
    }


  // ============================================================
  // PREDICT TRICKS
  // ============================================================

  private[control] def doPredictTricks(
                                        gs: GameState,
                                        predictions: Map[Int, Int]
                                      ): GameState =

    // ------------------------------------------------------------
    // Validate input
    // ------------------------------------------------------------
    val updatedPlayersOpt =
      gs.players.foldLeft(Option(List.empty[Player])) {
        case (None, _) => None

        case (Some(acc), player) =>
          predictions.get(player.id) match
            case None => None
            case Some(pred) if pred < 0 => None
            case Some(pred) =>
              Some(acc :+ player.copy(predictedTricks = pred))
      }

    // invalid retry
    if updatedPlayersOpt.isEmpty then
      gs.notifyObservers(StateChanged(gs))
      return gs

    val newState =
      gs.copy(players = updatedPlayersOpt.get)

    newState.notifyObservers(StateChanged(newState))
    newState

  // ============================================================
  // PLAY ONE TRICK
  // ============================================================

  private[control] def doPlayOneTrick(
                                       trickNumber: Int,
                                       gs: GameState,
                                       moves: Map[Int, Int]
                                     ): GameState =

    // ------------------------------------------------------------
    // Guard: no cards left
    // ------------------------------------------------------------
    if gs.players.exists(_.hand.isEmpty) then
      gs.notifyObservers(StateChanged(gs))
      return gs

    // Trick started
    gs.notifyObservers(TrickStarted(trickNumber, gs))

    // ------------------------------------------------------------
    // Phase 1: Validate & collect cards (no mutation)
    // ------------------------------------------------------------
    val collectedOpt =
      gs.players.foldLeft(Option(Trick(Map.empty[PlayerID, Card]))) {
        case (None, _) => None

        case (Some(trick), player) =>
          moves.get(player.id) match
            case None => None

            case Some(cardIndex) =>
              if cardIndex < 0 || cardIndex >= player.hand.size then
                None
              else
                val card = player.hand(cardIndex)
                if !isAllowedMove(card, player, trick) then
                  None
                else
                  Some(
                    Trick(trick.played + (player.id -> card))
                  )
      }

    // invalid trick → repeat
    if collectedOpt.isEmpty then
      gs.notifyObservers(StateChanged(gs))
      return gs

    val completedTrick = collectedOpt.get

    // ------------------------------------------------------------
    // Phase 2: Remove cards from hands
    // ------------------------------------------------------------
    val playersAfterRemoval =
      gs.players.map { p =>
        moves.get(p.id) match
          case Some(idx) =>
            p.copy(hand = p.hand.patch(idx, Nil, 1))
          case None =>
            p
      }

    val afterTrickState =
      gs.copy(
        players = playersAfterRemoval,
        currentTrick = Some(completedTrick)
      )

    afterTrickState.notifyObservers(StateChanged(afterTrickState))

    // ------------------------------------------------------------
    // Phase 3: Determine winner
    // ------------------------------------------------------------
    val (winnerId, _) =
      strategy.winner(completedTrick, afterTrickState.currentTrump)

    val playersAfterScoring =
      afterTrickState.players.map { p =>
        if p.id == winnerId then
          p.copy(tricks = p.tricks + 1)
        else
          p
      }

    val finalState =
      afterTrickState.copy(
        players = playersAfterScoring,
        currentTrick = None
      )

    finalState.notifyObservers(TrickFinished(winnerId, finalState))
    finalState



  // ============================================================
  // RULES
  // ============================================================

  private def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
    val leadOpt = trick.played.values.collectFirst {
      case c if isNormal(c) => c.color
    }

    if leadOpt.isEmpty then true
    else
      val lead = leadOpt.get
      val mustFollow =
        player.hand.exists(c => isNormal(c) && c.color == lead)

      if mustFollow then isNormal(card) && card.color == lead
      else true

  // ============================================================
  // SCORE ROUND
  // ============================================================

  private[control] def doScoreRound(gs: GameState): GameState =

    // ------------------------------------------------------------
    // Phase 1: calculate points
    // ------------------------------------------------------------
    val scoredPlayers =
      gs.players.map { p =>
        p.copy(
          totalPoints = p.totalPoints + calculateRoundPoints(p)
        )
      }

    val scoredState =
      gs.copy(players = scoredPlayers)

    // Event: round scored (before reset)
    scoredState.notifyObservers(RoundFinished(scoredState))

    // ------------------------------------------------------------
    // Phase 2: reset round-specific values
    // ------------------------------------------------------------
    val resetPlayers =
      scoredPlayers.map { p =>
        p.copy(tricks = 0, predictedTricks = 0)
      }

    val finalState =
      scoredState.copy(players = resetPlayers)

    finalState.notifyObservers(StateChanged(finalState))
    finalState


  private def calculateRoundPoints(p: Player): Int =
    if p.predictedTricks == p.tricks then
      20 + p.tricks * 10
    else
      p.tricks * 10 - 10 * (p.tricks - p.predictedTricks).abs

  // ============================================================
  // DETERMINE WINNER
  // ============================================================

  private[control] def doDetermineWinner(gs: GameState): GameState =
    val winner = gs.players.maxBy(_.totalPoints)

    gs.notifyObservers(GameFinished(winner, gs))
    gs

}
