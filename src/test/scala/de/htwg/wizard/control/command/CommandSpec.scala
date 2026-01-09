package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*

class CommandSpec extends AnyWordSpec with Matchers {

  "Command trait" should {

    "be usable via polymorphism and return a GameState" in {

      val dummyState = GameState(
        amountOfPlayers = 0,
        players = Nil,
        deck = Deck(),
        currentRound = 0,
        totalRounds = 0
      )

      val cmd: Command = new Command {
        override def execute(): GameState = dummyState
      }

      val result = cmd.execute()

      result shouldBe dummyState
    }
  }
}
