package de.htwg.wizard.control.command

import de.htwg.wizard.model.{Deck, GameState}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PrepareRoundCommandSpec extends AnyWordSpec with Matchers {

  "PrepareRoundCommand" should {
    "call prepareNextRound" in {
      val control = new FakeGameControl
      val state = GameState(0, Nil, Deck(), 0, 0)

      val cmd = new PrepareRoundCommand(control, state)
      cmd.execute()

      control.lastCalled shouldBe "prepareNextRound"
      control.lastArgs shouldBe state
    }
  }
}
