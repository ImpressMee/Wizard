package de.htwg.wizard.control.controlComponents.command

import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.GameState

object ScoreRoundCommand extends Command:

  override def execute(state: GameState): GameState =
    val scored =
      state.players.map { p =>
        val delta =
          if p.predictedTricks == p.tricks then
            20 + p.tricks * 10
          else
            -10 * (p.tricks - p.predictedTricks).abs

        p.copy(
          totalPoints = p.totalPoints + delta
        )
      }

    state.copy(players = scored)
