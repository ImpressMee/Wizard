package de.htwg.wizard

import de.htwg.wizard.control.GameController
import de.htwg.wizard.view.GameView
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class MainTest extends AnyWordSpec with Matchers:

  "The main method" should {
    "create a GameController and run the game without crashing" in {
      // Arrange
      val view = new GameView()
      val controller = new GameController(view)

      // Act & Assert
      noException should be thrownBy {
        // call method directly
        controller.initGame("3")
      }
    }
  }