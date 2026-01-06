package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

case class InitCommand(
                        control: GameControl,
                        playerCount: Int
                      ) extends Command:

  private var last: GameState = _

  override def execute(): GameState =
    control.saveState(control.currentState)
    val s = control.doInitGame(playerCount)
    last = s
    s

  override def undo(): GameState =
    control.undo(last)
