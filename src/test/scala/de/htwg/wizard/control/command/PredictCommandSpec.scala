package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*

class PredictCommandSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  val players =
    List(
      Player(id = 0, predictedTricks = 0),
      Player(id = 1, predictedTricks = 0),
      Player(id = 2, predictedTricks = 0)
    )

  val initialState =
    GameState(
      amountOfPlayers = 3,
      players = players,
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  val predictions = Map(
    0 -> 2,
    2 -> 1
  )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "PredictCommand" should {

    "set predictedTricks for players contained in the predictions map" in {
      val result =
        PredictCommand(predictions).execute(initialState)

      result.players.find(_.id == 0).get.predictedTricks shouldBe 2
      result.players.find(_.id == 2).get.predictedTricks shouldBe 1
    }

    "leave players without prediction unchanged" in {
      val result =
        PredictCommand(predictions).execute(initialState)

      result.players.find(_.id == 1).get.predictedTricks shouldBe 0
    }

    "not modify other player fields" in {
      val result =
        PredictCommand(predictions).execute(initialState)

      result.players.foreach { p =>
        p.hand shouldBe Nil
        p.tricks shouldBe 0
        p.totalPoints shouldBe 0
      }
    }

    "not mutate the original GameState" in {
      PredictCommand(predictions).execute(initialState)

      initialState.players.foreach(_.predictedTricks shouldBe 0)
    }

    "return a new GameState instance" in {
      val result =
        PredictCommand(predictions).execute(initialState)

      result should not be theSameInstanceAs(initialState)
    }
  }
}
