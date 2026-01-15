package de.htwg.wizard.control.controlComponent

import com.google.inject.Inject
import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponent.command.*
import de.htwg.wizard.control.controlComponent.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{GameState, Trick}
import de.htwg.wizard.persistence.FileIO

/**
 * Internal orchestration of the game.
 *
 * Responsibilities:
 * - Drive the game flow using a state machine
 * - Execute commands that transform the GameState
 * - Emit events for views (GUI / TUI)
 * - Manage undo/redo via Memento (state history)
 * - Persist state after every change
 */
class GameControl @Inject() (
                              strategy: TrickStrategy,
                              fileIO: FileIO
                            ) {

  // =========================================================
  // Observer
  // =========================================================

  private var observers: List[Observer] = Nil

  def registerObserver(o: Observer): Unit =
    observers ::= o

  private def notify(event: GameEvent): Unit =
    observers.foreach(_.update(event))

  // =========================================================
  // Internal state + Memento
  // =========================================================

  private var state: Option[GameState] = None
  private var phase: GameStatePhase = InitState

  private var history: List[GameState] = Nil
  private var future:  List[GameState] = Nil

  private def currentState: GameState =
    state.getOrElse(
      throw new IllegalStateException("GameControl used before start()")
    )

  private def setInitialState(initial: GameState): Unit = {
    state = Some(initial)
    history = Nil
    future = Nil
    fileIO.save(initial)
  }

  private def setState(newState: GameState): Unit = {
    history = currentState :: history
    future = Nil
    state = Some(newState)
    fileIO.save(newState)
  }

  // =========================================================
  // Lifecycle
  // =========================================================

  def init(): Unit = {
    notify(GameLoadAvailable(fileIO.hasSave, GameState.empty))
  }

  def start(initial: GameState): Unit = {
    setInitialState(initial)
    phase = InitState
    notify(GameLoadAvailable(fileIO.hasSave, initial))
    notify(PlayerAmountRequested(initial))
  }

  def loadGame(): Unit = {
    val loaded = fileIO.load()
    setInitialState(loaded)
    phase = phaseFromState(loaded)
    firePhase()
  }



  // =========================================================
  // Phase handling
  // =========================================================

  private def firePhase(): Unit = {
    val s = currentState

    phase match
      case InitState =>
        notify(PlayerAmountRequested(s))

      case PredictState =>
        notify(PredictionsRequested(s))

      case TrickState(n) =>
        notify(TrickStarted(n, s))
        notify(TrickMoveRequested(n, s))

      case ScoreState =>
        val scored = ScoreRoundCommand.execute(s)
        setState(scored)
        notify(RoundFinished(scored))

      case FinishState =>
        val winner = s.players.maxBy(_.totalPoints)
        notify(GameFinished(winner, s))
  }


  // =========================================================
  // Input handling
  // =========================================================

  def submitPlayerAmount(amount: Int): Unit = {
    val initialized =
      InitCommand(amount).execute(currentState)

    val prepared =
      PrepareRoundCommand.execute(initialized)
        .copy(completedTricks = 0)

    setState(prepared)
    phase = PredictState
    firePhase()
  }

  def submitPredictions(predictions: Map[Int, Int]): Unit = {
    val next =
      PredictCommand(predictions).execute(currentState)

    setState(next)
    phase = TrickState(1)
    firePhase()
  }

  def playTrick(moves: Map[Int, Int]): Unit = {

    val partialTrick =
      Trick(
        moves.map { case (pid, idx) =>
          val card =
            currentState.players.find(_.id == pid).get.hand(idx)
          pid -> card
        }
      )

    val stateWithTrick =
      currentState.copy(currentTrick = Some(partialTrick))

    val afterTrick =
      PlayTrickCommand(moves, strategy).execute(stateWithTrick)

    setState(afterTrick)

    phase =
      if afterTrick.players.head.hand.isEmpty
      then ScoreState
      else TrickState(afterTrick.completedTricks + 1)

    firePhase()
  }


  def prepareNextRound(): Unit = {
    val next =
      PrepareRoundCommand.execute(currentState)
        .copy(completedTricks = 0)

    setState(next)

    phase =
      if next.currentRound >= next.totalRounds
      then FinishState
      else PredictState

    firePhase()
  }

  def continueAfterRound(): Unit = {

    phase =
      if currentState.currentRound >= currentState.totalRounds then
        FinishState
      else
        PredictState

    firePhase()
  }

  // =========================================================
  // Undo / Redo (Memento)
  // =========================================================

  def undo(): Unit = {
    history match
      case prev :: rest =>
        future = currentState :: future
        history = rest
        state = Some(prev)
        fileIO.save(prev)
        notify(StateChanged(prev))
      case Nil => ()
  }

  def redo(): Unit = {
    future match
      case next :: rest =>
        history = currentState :: history
        future = rest
        state = Some(next)
        fileIO.save(next)
        notify(StateChanged(next))
      case Nil => ()
  }

  // =========================================================
  // Move validation
  // =========================================================

  def isAllowedMove(
                     playerId: Int,
                     cardIndex: Int,
                     state: GameState
                   ): Boolean = {

    val player =
      state.players.find(_.id == playerId)
        .getOrElse(return false)

    if cardIndex < 0 || cardIndex >= player.hand.size then
      return false

    val card = player.hand(cardIndex)

    state.currentTrick match
      case None => true
      case Some(trick) =>
        strategy.isAllowedMove(card, player, trick)
  }

  private def phaseFromState(state: GameState): GameStatePhase =
    if state.players.isEmpty then
      InitState
    else if state.currentTrick.isDefined then
      TrickState(state.completedTricks + 1)
    else if state.players.exists(_.predictedTricks < 0) then
      PredictState
    else
      ScoreState

  def canSafelyExit: Boolean =
    phase match
      case PredictState | TrickState(_) => false
      case _ => true

}
