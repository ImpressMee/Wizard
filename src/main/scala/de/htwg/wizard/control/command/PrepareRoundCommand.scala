package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.{CardColor, GameState}

case class PrepareRoundCommand(
                                control: GameControl,
                                state: GameState,
                                trump: Option[CardColor]
                              ) extends Command:

  override def execute(): GameState =
    control.doPrepareNextRound(state, trump)


  override def undo(): GameState =
    control.undo(state)
