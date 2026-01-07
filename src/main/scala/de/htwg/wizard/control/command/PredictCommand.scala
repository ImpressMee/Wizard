package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

class PredictCommand(
                      control: GameControl,
                      state: GameState,
                      predictions: Map[Int, Int]
                    ) extends Command:

  override def execute(): GameState =
    control.predictTricks(state, predictions)
