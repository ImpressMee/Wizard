package de.htwg.wizard.control

import de.htwg.wizard.model.GameState

trait GameStatePhase {
  def next(state: GameState): GameStatePhase
}

case object InitState extends GameStatePhase {
  override def next(state: GameState): GameStatePhase =
    this
}

case object PredictState extends GameStatePhase {
  override def next(state: GameState): GameStatePhase =
    if state.players.forall(_.predictedTricks >= 0)
    then TrickState(1)
    else this
}

case class TrickState(trickNr: Int) extends GameStatePhase {
  override def next(state: GameState): GameStatePhase =
    if state.players.headOption.exists(_.hand.isEmpty)
    then ScoreState
    else this
}

case object ScoreState extends GameStatePhase {
  override def next(state: GameState): GameStatePhase =
    this   // <<< WICHTIG: BLOCKIEREND
}

case object FinishState extends GameStatePhase {
  override def next(state: GameState): GameStatePhase =
    this
}
