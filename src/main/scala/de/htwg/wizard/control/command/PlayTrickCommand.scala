package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.GameState

class PlayTrickCommand(
                        control: GameControl,
                        trickNr: Int,
                        state: GameState,
                        moves: Map[Int, Int]
                      ) extends Command:

  override def execute(): GameState =
    control.playOneTrick(trickNr, state, moves)
