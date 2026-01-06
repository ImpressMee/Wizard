package de.htwg.wizard

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.view.TuiView

object main:

  def main(args: Array[String]): Unit =
    val control = new GameControl()
    val view    = new TuiView

    // Spiel starten (View wird intern als Observer registriert)
    control.runGame(view)
