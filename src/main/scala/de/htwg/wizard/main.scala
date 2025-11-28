package de.htwg.wizard

import de.htwg.wizard.control.*
import de.htwg.wizard.view.*

object main:

  def main(args: Array[String]): Unit =
    //val altStrategy = new AlternativeTrickStrategy
    startWizard(new GameView())

  def startWizard(view: GameView): Unit =
    new GameControl(view).runGame()

  def startWizard(view: GameView, strategy: TrickStrategy): Unit =
    new GameControl(view, strategy).runGame()
