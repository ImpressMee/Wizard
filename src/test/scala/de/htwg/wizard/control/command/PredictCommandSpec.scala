package de.htwg.wizard.control.command

import scala.util.{Try, Success}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*

class PredictCommandSpec extends AnyWordSpec with Matchers {

  "PredictCommand" should {
    "call predictTricks with predictions" in {
      val control = new FakeGameControl
      val state = GameState(0, Nil, Deck(), 0, 0)
      val predictions = Map(0 -> 2, 1 -> 1)

      val cmd = new PredictCommand(control, state, predictions)
      cmd.execute()

      control.lastCalled shouldBe "predictTricks"
      control.lastArgs shouldBe (state, predictions)
    }
  }
}
