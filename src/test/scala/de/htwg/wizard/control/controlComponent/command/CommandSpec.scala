package de.htwg.wizard.control.controlComponent.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.modelComponent.GameState

class CommandSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Dummy Command for contract testing
  // ---------------------------------------------------------
  object DummyCommand extends Command {
    override def execute(state: GameState): GameState =
      state.copy(currentRound = state.currentRound + 1)
  }

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  val initialState: GameState =
    GameState.empty.copy(currentRound = 1)

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "A Command" should {

    "transform a GameState into a new GameState" in {
      val result = DummyCommand.execute(initialState)

      result shouldBe a[GameState]
      result.currentRound shouldBe 2
    }

    "not mutate the original GameState" in {
      DummyCommand.execute(initialState)

      initialState.currentRound shouldBe 1
    }

    "return a different instance than the input state" in {
      val result = DummyCommand.execute(initialState)

      result should not be theSameInstanceAs(initialState)
    }
  }
}
