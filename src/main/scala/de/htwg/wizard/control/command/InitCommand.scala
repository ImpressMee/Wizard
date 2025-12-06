package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

case class InitCommand(control: GameControl) extends Command:
  private var last: GameState = _

  override def execute(): GameState =
    val s = control.doInitGame()
    control.saveState(s)
    last = s
    s

  override def undo(): GameState =
    control.undo(last)
