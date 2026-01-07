package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

class InitCommand(
                   control: GameControl,
                   playerCount: Int
                 ) extends Command:

  override def execute(): GameState =
    control.initGame(playerCount)
