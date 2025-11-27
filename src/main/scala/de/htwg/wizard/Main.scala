package de.htwg.wizard

import de.htwg.wizard.control.*
import de.htwg.wizard.view.*

object Main:

  def main(): Unit =
    startWizard()

  def startWizard(): Unit =
    val view = new GameView()
    val controller = new GameControl(view)
    controller.runGame()
