package de.htwg.wizard.control.controlComponents

import de.htwg.wizard.control.controlComponents.{ScoreState, TrickState}
import de.htwg.wizard.model.modelComponent.GameState

case object InitState extends GameStatePhase {
  def next(state: GameState): GameStatePhase = this
}

case object PredictState extends GameStatePhase {
  def next(state: GameState): GameStatePhase =
    if state.players.forall(_.predictedTricks >= 0)
    then TrickState(1)
    else this
}

case class TrickState(trickNr: Int) extends GameStatePhase {
  def next(state: GameState): GameStatePhase =
    if state.players.headOption.exists(_.hand.isEmpty)
    then ScoreState
    else this
}

case object ScoreState extends GameStatePhase {
  def next(state: GameState): GameStatePhase = this
}

case object FinishState extends GameStatePhase {
  override def next(state: GameState): GameStatePhase =
    this
}