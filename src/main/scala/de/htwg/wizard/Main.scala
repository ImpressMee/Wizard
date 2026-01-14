package de.htwg.wizard

import scalafx.application.JFXApp3
import com.google.inject.Guice
import de.htwg.wizard.di.StandardModule
import de.htwg.wizard.control.GamePort
import de.htwg.wizard.view.{GuiView, TuiView}

object Main extends JFXApp3{

  override def start(): Unit = {

    // ---------------------------------------------------------
    // Dependency Injection (Composition Root)
    // ---------------------------------------------------------
    val injector =
      Guice.createInjector(new StandardModule)

    val game: GamePort =
      injector.getInstance(classOf[GamePort])

    // ---------------------------------------------------------
    // Views
    // ---------------------------------------------------------
    val gui = new GuiView(game)
    val tui = new TuiView()

    game.registerObserver(gui)
    game.registerObserver(tui)

    stage = new JFXApp3.PrimaryStage {
      title = "Wizard"
      resizable = false
    }

    gui.showStart(stage)
  }
}
