package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.modelComponent.*

class ModelInterfaceSpec extends AnyWordSpec with Matchers {

  "A ModelInterface implementation" should {

    "provide an initial game state" in {
      val model: ModelInterface = new ModelComponent()

      model.state shouldBe GameState.empty
    }

    "update the current state" in {
      val model: ModelInterface = new ModelComponent()
      val newState = GameState.empty.copy(currentRound = 1)

      model.updateState(newState)

      model.state shouldBe newState
    }

    "restore the previous state after undo" in {
      val model: ModelInterface = new ModelComponent()

      val state1 = GameState.empty
      val state2 = GameState.empty.copy(currentRound = 1)

      model.updateState(state1)
      model.save()

      model.updateState(state2)
      model.undo()

      model.state shouldBe state1
    }
  }
}
