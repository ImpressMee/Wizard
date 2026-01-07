package de.htwg.wizard

import scalafx.application.JFXApp3
import de.htwg.wizard.control.GameControl
import de.htwg.wizard.view.{GuiView, TuiView}

object Main extends JFXApp3 {

  override def start(): Unit =
    val control = new GameControl()
    val gui = new GuiView(control)
    val tui = new TuiView()

    stage = new JFXApp3.PrimaryStage {
      title = "Wizard"
      resizable = false
    }

    control.registerObservers(gui, tui)
    gui.showStart(stage)
}
