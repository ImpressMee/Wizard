package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

class DetermineWinnerCommand(
                              control: GameControl,
                              state: GameState
                            ) extends Command:

  override def execute(): GameState =
    control.determineWinner(state)
