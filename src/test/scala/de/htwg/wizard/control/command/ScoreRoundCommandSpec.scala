package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import scala.util.{Try, Success}

class ScoreRoundCommandSpec extends AnyWordSpec with Matchers {

  "ScoreRoundCommand" should {
    "call scoreRound" in {
      val control = new FakeGameControl
      val state = GameState(0, Nil, Deck(), 0, 0)

      val cmd = new ScoreRoundCommand(control, state)
      cmd.execute()

      control.lastCalled shouldBe "scoreRound"
      control.lastArgs shouldBe state
    }
  }
}
