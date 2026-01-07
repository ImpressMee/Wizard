package de.htwg.wizard.control.event

import de.htwg.wizard.model.{Card, GameState, Player}

sealed trait GameEvent {
  def state: GameState
}

case class RoundStarted(round: Int, state: GameState) extends GameEvent
case class TrickStarted(n: Int, state: GameState) extends GameEvent
case class TrickFinished(winnerId: Int, state: GameState) extends GameEvent
case class RoundFinished(state: GameState) extends GameEvent
case class GameFinished(winner: Player, state: GameState) extends GameEvent
case class StateChanged(state: GameState) extends GameEvent
// Input-Anforderungen
case class PlayerAmountRequested(state: GameState) extends GameEvent
case class PredictionsRequested(state: GameState) extends GameEvent
case class TrickMoveRequested(trickNr: Int, state: GameState) extends GameEvent
