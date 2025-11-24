package de.htwg.wizard

import de.htwg.wizard.control.*
import de.htwg.wizard.view.GameView


/**
 * @author Justin-Jay Balaba
 * @author Nikita Kusch
*/

@main def main(): Unit =
  val view = new GameView()
  val controller = new GameControl(view)

  controller.runGame()