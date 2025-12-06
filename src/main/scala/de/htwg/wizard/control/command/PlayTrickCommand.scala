package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

case class PlayTrickCommand(control: GameControl, n: Int, gs: GameState) extends Command:
  private var last: GameState = gs

  override def execute(): GameState =
    control.saveState(gs)
    val s2 = control.doPlayOneTrick(n, gs)
    last = s2
    s2

  override def undo(): GameState =
    control.undo(last)
