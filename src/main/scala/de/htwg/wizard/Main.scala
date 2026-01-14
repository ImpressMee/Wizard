package de.htwg.wizard

import scalafx.application.JFXApp3
import de.htwg.wizard.control.GamePort
import de.htwg.wizard.control.controlComponents.component.GameComponent
import de.htwg.wizard.model.ModelInterface
import de.htwg.wizard.model.modelComponent.ModelComponent
import de.htwg.wizard.view.{GuiView, TuiView}

/**
 * Main is the composition root of the application.
 *
 * Responsibilities:
 * - Instantiate components
 * - Wire dependencies
 * - Do not contain any game logic
 *
 * Architectural role:
 * - Entry point
 * - Dependency Injection container (manual)
 */

object Main extends JFXApp3 {

  override def start(): Unit =

    val model: ModelInterface = new ModelComponent()
    val game: GamePort = new GameComponent(model)

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

