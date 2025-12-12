package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.GameState
import de.htwg.wizard.model.*

class CommandSpec extends AnyWordSpec with Matchers {

  "Command trait" should {

    "allow execute and undo to be called" in {

      // Dummy GameState
      val gs = GameState(
        amountOfPlayers = 0,
        players = Nil,
        deck = new Deck(),
        currentRound = 0,
        totalRounds = 0,
        currentTrump = None
      )

      // Anonyme Implementierung des Traits
      val command = new Command {
        override def execute(): GameState = gs
        override def undo(): GameState = gs
      }

      // Beide Methoden ausführen → 100 % Coverage
      command.execute() shouldBe gs
      command.undo() shouldBe gs
    }
  }
}
