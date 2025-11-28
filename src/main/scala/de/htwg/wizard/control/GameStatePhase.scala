package de.htwg.wizard.control

import de.htwg.wizard.model.*

trait GameStatePhase:
  def run(control: GameControl, state: GameState): (GameStatePhase, GameState)
// -------------------------
// Singleton States
// -------------------------

case object InitState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    val afterInit  = control.initGame()
    val afterRound = control.prepareNextRound(afterInit)
    (PredictState, afterRound)

case object PrepareRoundState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    val s2 = control.prepareNextRound(state)
    if s2.currentRound > s2.totalRounds then
      (FinishState, s2)
    else
      (PredictState, s2)

case object PredictState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    val s2 = control.predictTricks(state)
    (TrickState(1), s2)

case object ScoreState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    val s2 = control.scoreRound(state)
    (PrepareRoundState, s2)

case object FinishState extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    control.finishGame(state)
    (null, state)


// -------------------------
// Stateful Phase (needs parameter)
// -------------------------

case class TrickState(n: Int) extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    if n > state.players.head.hand.size then
      (ScoreState, state)
    else
      val s2 = control.playOneTrick(n, state)
      (TrickState(n + 1), s2)
