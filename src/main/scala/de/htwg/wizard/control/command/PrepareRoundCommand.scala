package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

case class PrepareRoundCommand(control: GameControl, state: GameState) extends Command:

  override def execute(): GameState =
    val prepared = control.doPrepareNextRound(state)
    prepared.notifyObservers()
    prepared

  override def undo(): GameState =
    control.undo(state)
