package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import scala.util.{Try, Success}

class DetermineWinnerCommandSpec extends AnyWordSpec with Matchers {

  "DetermineWinnerCommand" should {
    "call determineWinner" in {
      val control = new FakeGameControl
      val state = GameState(0, Nil, Deck(), 0, 0)

      val cmd = new DetermineWinnerCommand(control, state)
      cmd.execute()

      control.lastCalled shouldBe "determineWinner"
      control.lastArgs shouldBe state
    }
  }
}
