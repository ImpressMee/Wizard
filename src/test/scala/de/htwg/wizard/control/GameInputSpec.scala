package de.htwg.wizard.control

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameInputSpec extends AnyWordSpec with Matchers {

  "GameInput ADT" should {

    "allow creation of PlayerAmountSelected" in {
      val in: GameInput = PlayerAmountSelected(4)
      in shouldBe PlayerAmountSelected(4)
    }

    "allow creation of PredictionsSubmitted" in {
      val in: GameInput = PredictionsSubmitted(Map(0 -> 2, 1 -> 1))
      in shouldBe PredictionsSubmitted(Map(0 -> 2, 1 -> 1))
    }

    "allow creation of TrickMovesSubmitted" in {
      val in: GameInput = TrickMovesSubmitted(Map(0 -> 0))
      in shouldBe TrickMovesSubmitted(Map(0 -> 0))
    }

    "support ContinueAfterRound" in {
      val in: GameInput = ContinueAfterRound
      in shouldBe ContinueAfterRound
    }

    "support Undo" in {
      val in: GameInput = Undo
      in shouldBe Undo
    }

    "support Redo" in {
      val in: GameInput = Redo
      in shouldBe Redo
    }

    "support LoadGame" in {
      val in: GameInput = LoadGame
      in shouldBe LoadGame
    }
  }
}
