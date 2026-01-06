package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.control.command.*
import de.htwg.wizard.control.event.*

trait GameStatePhase:
  def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState)


// ============================================================
// INIT
// ============================================================

case object InitState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    state.notifyObservers(PlayerAmountRequested(state))
    (Some(InitState), state)



// ============================================================
// PREPARE NEXT ROUND
// ============================================================

case object PrepareRoundState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    state.notifyObservers(TrumpSelectionRequested(state))
    (Some(PrepareRoundState), state)


// ============================================================
// PREDICT TRICKS
// ============================================================

case object PredictState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    state.notifyObservers(PredictionsRequested(state))
    (Some(PredictState), state)


// ============================================================
// TRICK STATE (STATEFUL)
// ============================================================

case class TrickState(n: Int) extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    state.notifyObservers(TrickMoveRequested(n, state))
    (Some(TrickState(n)), state)

// ============================================================
// SCORE ROUND
// ============================================================

case object ScoreState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    val next = control.doScoreRound(state)
    (Some(PrepareRoundState), next)

// ============================================================
// FINISH
// ============================================================

case object FinishState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    control.doDetermineWinner(state)
    (None, state)
