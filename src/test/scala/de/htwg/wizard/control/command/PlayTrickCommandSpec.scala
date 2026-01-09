package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Try, Success}
import de.htwg.wizard.control.GameControl
import de.htwg.wizard.control.strategy.TrickStrategy
import de.htwg.wizard.model.*


class PlayTrickCommandSpec extends AnyWordSpec with Matchers {

  "PlayTrickCommand" should {
    "call playOneTrick with trick number and moves" in {
      val control = new FakeGameControl
      val state = GameState(0, Nil, Deck(), 0, 0)
      val moves = Map(0 -> 1)

      val cmd = new PlayTrickCommand(control, 2, state, moves)
      cmd.execute()

      control.lastCalled shouldBe "playOneTrick"
      control.lastArgs shouldBe (2, state, moves)
    }
  }
}
