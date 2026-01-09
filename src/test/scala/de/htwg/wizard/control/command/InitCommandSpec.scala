package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class InitCommandSpec extends AnyWordSpec with Matchers {

  "InitCommand" should {
    "call initGame on GameControl" in {
      val control = new FakeGameControl
      val cmd = new InitCommand(control, 4)

      val result = cmd.execute()

      control.lastCalled shouldBe "initGame"
      control.lastArgs shouldBe 4
      result shouldBe a [de.htwg.wizard.model.GameState]
    }
  }
}
