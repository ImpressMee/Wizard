package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.control.event.*

trait GameStatePhase:
  def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState)


// ============================================================
// INIT
// ============================================================

case object InitState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    println("[STATE] InitState")
    state.notifyObservers(PlayerAmountRequested(state))
    (None, state)


// ============================================================
// PREPARE NEXT ROUND
// ============================================================

case object PrepareRoundState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    println("[STATE] PrepareRoundState")
    (None, state)

// ============================================================
// PREDICT TRICKS
// ============================================================

case object PredictState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    println("[STATE] PredictState")
    state.notifyObservers(PredictionsRequested(state))
    (None, state)

// ============================================================
// TRICK STATE (STATEFUL)
// ============================================================

case class TrickState(n: Int) extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    println(s"[STATE] TrickState($n)")
    state.notifyObservers(TrickMoveRequested(n, state))
    (None, state)


// ============================================================
// SCORE ROUND
// ============================================================

case object ScoreState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    println("[STATE] ScoreState")
    val next = control.doScoreRound(state)
    (Some(PrepareRoundState), next)


// ============================================================
// FINISH
// ============================================================

case object FinishState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    println("[STATE] FinishState")
    control.doDetermineWinner(state)
    (None, state)
