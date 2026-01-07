package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

case class InitCommand(
                        control: GameControl,
                        playerCount: Int
                      ) extends Command:

  private var before: Option[GameState] = None

  override def execute(): GameState =
    before = control.currentState
    control.currentState.foreach(control.saveState)

    val newState = control.doInitGame(playerCount)
    newState

  override def undo(): GameState =
    before match
      case Some(state) => state
      case None        => throw new IllegalStateException("Nothing to undo")
