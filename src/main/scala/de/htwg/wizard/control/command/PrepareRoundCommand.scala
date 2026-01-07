package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.{CardColor, GameState}

case class PrepareRoundCommand(
                                control: GameControl,
                                state: GameState,
                              ) extends Command:

  override def execute(): GameState =
    control.doPrepareNextRound(state)


  override def undo(): GameState =
    control.undo(state)
