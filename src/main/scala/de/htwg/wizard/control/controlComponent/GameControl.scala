package de.htwg.wizard.control.controlComponent

import com.google.inject.Inject
import de.htwg.wizard.control.controlComponent.*
import de.htwg.wizard.control.controlComponent.command.*
import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponent.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.GameState

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
class GameControl @Inject() (
                              model: ModelInterface,
                              strategy: TrickStrategy
                            ) {

  private var observers: List[Observer] = Nil

  def registerObserver(o: Observer): Unit =
    observers ::= o

  private def notify(event: GameEvent): Unit =
    observers.foreach(_.update(event))
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

  private def firePhase(): Unit = {
    val s = currentState

    phase match
      case InitState =>
        notify(PlayerAmountRequested(s))

      case PredictState =>
        notify(PredictionsRequested(s))

      case TrickState(n) =>
        notify(TrickStarted(n, s))
        println(currentState.completedTricks)
        notify(TrickMoveRequested(n, s))

      case ScoreState =>
        state = Some(ScoreRoundCommand.execute(s))
        notify(RoundFinished(currentState))

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

    state = Some(prepared)
    phase = PredictState
    firePhase()
  }

  def submitPredictions(predictions: Map[Int, Int]): Unit =
    state = Some(PredictCommand(predictions).execute(currentState))
    phase = TrickState(1)
    firePhase()

  def playTrick(moves: Map[Int, Int]): Unit = {
    val afterTrick =
      PlayTrickCommand(moves, strategy).execute(currentState)

    state = Some(afterTrick)

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

    state = Some(next)

    phase =
      if next.currentRound >= next.totalRounds
      then FinishState
      else PredictState

    firePhase()
  }

  // =========================================================
  // Undo / Redo hooks (optional, UI compatibility)
  // =========================================================

  def undo(): Unit =
    notify(StateChanged(currentState))

  def redo(): Unit =
    notify(StateChanged(currentState))

  // =========================================================
  // Move validation (used by Component-GUI)
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
      case None =>
        true // erste Karte im Stich immer erlaubt

      case Some(trick) =>
        strategy.isAllowedMove(
          card = card,
          player = player,
          trick = trick
        )
  }
}
