package de.htwg.wizard.model.modelComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.ModelInterface

class ModelComponentSpec extends AnyWordSpec with Matchers {

  "ModelComponent" should {

    val model: ModelInterface = new ModelComponent

    "start with empty initial state" in {
      model.state.amountOfPlayers shouldBe 0
      model.state.players shouldBe Nil
    }

    "update state correctly" in {
      val s = GameState.empty.copy(amountOfPlayers = 4)
      model.updateState(s)
      model.state.amountOfPlayers shouldBe 4
    }

    "save state to history" in {
      noException shouldBe thrownBy(model.save())
    }

    "undo to previous state" in {
      val original = model.state

      model.save() // Snapshot VOR Änderung
      model.updateState(original.copy(amountOfPlayers = 2))
      model.undo()

      model.state.amountOfPlayers shouldBe original.amountOfPlayers
    }


    "do nothing on undo with empty history" in {
      val fresh = new ModelComponent
      noException shouldBe thrownBy(fresh.undo())
    }
  }
}
