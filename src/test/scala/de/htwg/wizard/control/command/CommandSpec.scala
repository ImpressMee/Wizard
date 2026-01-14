package de.htwg.wizard.control.command

import de.htwg.wizard.control.controlComponents.command.Command
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Deck, GameState, Player}

class CommandSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Dummy Command for testing the Command contract
  // ---------------------------------------------------------
  case object DummyCommand extends Command {
    override def execute(state: GameState): GameState =
      state.copy(currentRound = state.currentRound + 1)
  }

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  val initialState: GameState =
    GameState(
      amountOfPlayers = 3,
      players = List(Player(0), Player(1), Player(2)),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "A Command" should {

    "return a new GameState when executed" in {
      val newState = DummyCommand.execute(initialState)

      newState shouldBe a [GameState]
      newState.currentRound shouldBe 2
    }

    "not mutate the original GameState" in {
      DummyCommand.execute(initialState)

      initialState.currentRound shouldBe 1
    }

    "follow the Command contract (GameState => GameState)" in {
      val result = DummyCommand.execute(initialState)

      result should not be theSameInstanceAs(initialState)
    }
  }
}
