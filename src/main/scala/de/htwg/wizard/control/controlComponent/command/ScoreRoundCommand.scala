package de.htwg.wizard.control.controlComponent.command

import de.htwg.wizard.control.controlComponent.command.Command
import de.htwg.wizard.model.modelComponent.GameState

object ScoreRoundCommand extends Command {

  override def execute(state: GameState): GameState = {

    val updatedPlayers =
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

    state.copy(
      players         = updatedPlayers,
      currentTrick    = None,
      completedTricks = 0
    )
  }
}
