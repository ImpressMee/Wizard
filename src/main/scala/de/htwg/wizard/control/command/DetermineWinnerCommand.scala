package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

case class DetermineWinnerCommand(control: GameControl, gs: GameState) extends Command:

  override def execute(): GameState =
    control.doDetermineWinner(gs)
    gs

  override def undo(): GameState =
    gs   // no undo possible
