package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.control.command.*
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.strategy.TrickStrategy

/**
 * Internal orchestration of the game.
 *
 * Responsibilities:
 * - Drive the game flow using a state machine
 * - Execute commands that transform the GameState
 * - Emit events for views (GUI / TUI)
 *
 * Notes:
 * - No UI logic
 * - No duplicated game state
 * - All persistent state lives in GameState
 */
class GameControl(
                   strategy: TrickStrategy,
                   notify: GameEvent => Unit
                 ) {

  // =========================================================
  // Internal state
  // =========================================================

  private var state: Option[GameState] = None
  private var phase: GameStatePhase = InitState

  private def currentState: GameState =
    state.getOrElse(
      throw new IllegalStateException("GameControl used before start()")
    )

  // =========================================================
  // Lifecycle
  // =========================================================

  def start(initial: GameState): Unit =
    state = Some(initial)
    phase = InitState
    firePhase()

  // =========================================================
  // Phase handling
  // =========================================================

  private def firePhase(): Unit =
    phase match

      case InitState =>
        notify(PlayerAmountRequested(currentState))

      case PredictState =>
        notify(PredictionsRequested(currentState))

      case TrickState(n) =>
        notify(TrickStarted(n, currentState))
        notify(TrickMoveRequested(n, currentState))

      case ScoreState =>
        state = Some(ScoreRoundCommand.execute(currentState))
        notify(RoundFinished(currentState))

      case FinishState =>
        val winner = currentState.players.maxBy(_.totalPoints)
        notify(GameFinished(winner, currentState))

  // =========================================================
  // Input handling
  // =========================================================

  def submitPlayerAmount(amount: Int): Unit =
    // initialize players and deck
    state = Some(InitCommand(amount).execute(currentState))

    // prepare first round (deal cards, set trump, reset trick counter)
    state = Some(
      PrepareRoundCommand.execute(currentState)
        .copy(completedTricks = 0)
    )

    phase = PredictState
    firePhase()

  def submitPredictions(predictions: Map[Int, Int]): Unit =
    state = Some(PredictCommand(predictions).execute(currentState))
    phase = TrickState(1)
    firePhase()

  def playTrick(moves: Map[Int, Int]): Unit =
    val afterTrick =
      PlayTrickCommand(moves, strategy).execute(currentState)

    // increase completed trick counter
    val updated =
      afterTrick.copy(
        completedTricks = currentState.completedTricks + 1,
        currentTrick = None
      )

    state = Some(updated)

    // decide next phase
    if updated.players.head.hand.isEmpty then
      phase = ScoreState
    else
      phase = TrickState(updated.completedTricks + 1)

    firePhase()

  def prepareNextRound(): Unit =
    val next =
      PrepareRoundCommand.execute(currentState)
        .copy(completedTricks = 0)

    state = Some(next)

    phase =
      if next.currentRound >= next.totalRounds
      then FinishState
      else PredictState

    firePhase()

  // =========================================================
  // Undo / Redo hooks (optional, UI compatibility)
  // =========================================================

  def undo(): Unit =
    notify(StateChanged(currentState))

  def redo(): Unit =
    notify(StateChanged(currentState))
}
