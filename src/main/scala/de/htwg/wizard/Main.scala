package de.htwg.wizard

import de.htwg.wizard.control.GameController
import de.htwg.wizard.view.GameView
import scala.io.StdIn.*

/**
 * @author Justin-Jay Balaba
 * @author Nikita Kusch
*/

@main def main(): Unit =
  val view = new GameView()
  val controller = new GameController(view)

  controller.runGame()