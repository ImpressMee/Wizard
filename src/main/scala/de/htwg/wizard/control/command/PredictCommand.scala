package de.htwg.wizard.control.command

import de.htwg.wizard.model.*

case class PredictCommand(predictions: Map[Int, Int]) extends Command:

  override def execute(state: GameState): GameState =
    val updated =
      state.players.map { p =>
        predictions.get(p.id) match
          case Some(v) => p.copy(predictedTricks = v)
          case None    => p
      }

    state.copy(players = updated)
