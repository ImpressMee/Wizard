package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.control.command.*

trait GameStatePhase:
  def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState)


// ============================================================
// INIT
// ============================================================

case object InitState extends GameStatePhase:
  override def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState) =
    val initCmd   = InitCommand(control)
    val afterInit = initCmd.execute()

    val prepCmd   = PrepareRoundCommand(control, afterInit)
    val afterPrep = prepCmd.execute()

    (Some(PredictState), afterPrep)


// ============================================================
// PREPARE NEXT ROUND
// ============================================================

case object PrepareRoundState extends GameStatePhase:
  override def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState) =
    val cmd = PrepareRoundCommand(control, state)
    val s2  = cmd.execute()

    if s2.currentRound >= s2.totalRounds then
      (Some(FinishState), s2)
    else
      (Some(PredictState), s2)



// ============================================================
// PREDICT TRICKS
// ============================================================

case object PredictState extends GameStatePhase:
  override def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState) =
    val cmd = PredictCommand(control, state)
    val s2  = cmd.execute()
    (Some(TrickState(1)), s2)


// ============================================================
// TRICK STATE (STATEFUL)
// ============================================================

case class TrickState(n: Int) extends GameStatePhase:
  override def run(control: GameControl, state: GameState) =
    val beforeHandSize = state.players.head.hand.size

    val cmd       = PlayTrickCommand(control, n, state)
    val nextState = cmd.execute()

    val afterHandSize = nextState.players.head.hand.size

    // if no card was removed, the trick was invalid → repeat same trick number
    if afterHandSize == beforeHandSize then
      (Some(TrickState(n)), state)
    else if afterHandSize == 0 then
      (Some(ScoreState), nextState)
    else
      (Some(TrickState(n + 1)), nextState)



// ============================================================
// SCORE ROUND
// ============================================================

case object ScoreState extends GameStatePhase:
  override def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState) =
    val cmd = ScoreRoundCommand(control, state)
    val s2  = cmd.execute()
    (Some(PrepareRoundState), s2)


// ============================================================
// FINISH
// ============================================================

case object FinishState extends GameStatePhase:
  override def run(control: GameControl, state: GameState): (Option[GameStatePhase], GameState) =
    val cmd = DetermineWinnerCommand(control, state)
    cmd.execute()
    (None, state)
