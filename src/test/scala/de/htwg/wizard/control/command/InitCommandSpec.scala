package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*

class InitCommandSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Helper: dummy initial state (will be replaced completely)
  // ---------------------------------------------------------
  val emptyState: GameState =
    GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = Deck(),
      currentRound = -1,
      totalRounds = 0
    )

  "InitCommand" should {

    "initialize the correct number of players" in {
      val cmd = InitCommand(4)
      val state = cmd.execute(emptyState)

      state.amountOfPlayers shouldBe 4
      state.players.size shouldBe 4
    }

    "assign player IDs from 0 to playerCount-1" in {
      val cmd = InitCommand(5)
      val state = cmd.execute(emptyState)

      state.players.map(_.id) shouldBe List(0, 1, 2, 3, 4)
    }

    "start the game at round 0" in {
      val cmd = InitCommand(3)
      val state = cmd.execute(emptyState)

      state.currentRound shouldBe 0
    }

    "calculate totalRounds correctly depending on player count" in {
      InitCommand(3).execute(emptyState).totalRounds shouldBe 4
      InitCommand(4).execute(emptyState).totalRounds shouldBe 3
      InitCommand(5).execute(emptyState).totalRounds shouldBe 2
      InitCommand(6).execute(emptyState).totalRounds shouldBe 2
    }

    "create a fresh shuffled deck" in {
      val cmd = InitCommand(4)
      val state = cmd.execute(emptyState)

      state.deck.cards.nonEmpty shouldBe true
    }

    "not mutate the original GameState" in {
      InitCommand(4).execute(emptyState)

      emptyState.amountOfPlayers shouldBe 0
      emptyState.players shouldBe Nil
      emptyState.currentRound shouldBe -1
    }

    "return a new GameState instance" in {
      val result = InitCommand(4).execute(emptyState)

      result should not be theSameInstanceAs(emptyState)
    }
  }
}
