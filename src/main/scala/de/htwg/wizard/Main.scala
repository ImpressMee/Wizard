package de.htwg.wizard

import scalafx.application.JFXApp3
import de.htwg.wizard.component.game.{GameComponent, GamePort}
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

    // ---------------------------------------------------------
    // Create game component (single component instance)
    // ---------------------------------------------------------

    val game: GamePort =
      new GameComponent()

    // ---------------------------------------------------------
    // Create views (depend only on GamePort)
    // ---------------------------------------------------------

    val gui = new GuiView(game)
    val tui = new TuiView()

    // ---------------------------------------------------------
    // Register observers
    // ---------------------------------------------------------

    game.registerObserver(gui)
    game.registerObserver(tui)

    // ---------------------------------------------------------
    // JavaFX stage setup
    // ---------------------------------------------------------

    stage = new JFXApp3.PrimaryStage {
      title = "Wizard"
      resizable = false
    }

    // ---------------------------------------------------------
    // Start UI
    // ---------------------------------------------------------

    gui.showStart(stage)
}
