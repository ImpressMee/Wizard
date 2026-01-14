package de.htwg.wizard.control.controlComponent.command

import de.htwg.wizard.control.controlComponent.command.ScoreRoundCommand
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Deck, GameState, Player}

class ScoreRoundCommandSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  val players =
    List(
      // exact prediction: 2 tricks
      Player(
        id = 0,
        tricks = 2,
        predictedTricks = 2,
        totalPoints = 10
      ),

      // over-prediction: predicted 2, got 1
      Player(
        id = 1,
        tricks = 1,
        predictedTricks = 2,
        totalPoints = 20
      ),

      // under-prediction: predicted 0, got 3
      Player(
        id = 2,
        tricks = 3,
        predictedTricks = 0,
        totalPoints = 0
      )
    )

  val initialState =
    GameState(
      amountOfPlayers = 3,
      players = players,
      deck = Deck(),
      currentRound = 2,
      totalRounds = 5
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "ScoreRoundCommand" should {

    "award correct points for exact prediction" in {
      val result =
        ScoreRoundCommand.execute(initialState)

      // 20 + 2 * 10 = 40 points
      result.players.find(_.id == 0).get.totalPoints shouldBe 50
    }

    "apply correct penalty for incorrect prediction (absolute difference)" in {
      val result =
        ScoreRoundCommand.execute(initialState)

      // |1 - 2| = 1 → -10
      result.players.find(_.id == 1).get.totalPoints shouldBe 10

      // |3 - 0| = 3 → -30
      result.players.find(_.id == 2).get.totalPoints shouldBe -30
    }

    "reset tricks and predictedTricks after scoring" in {
      val result =
        ScoreRoundCommand.execute(initialState)

      result.players.foreach { p =>
        p.tricks shouldBe 0
        p.predictedTricks shouldBe 0
      }
    }

    "not mutate the original GameState" in {
      ScoreRoundCommand.execute(initialState)

      initialState.players.find(_.id == 0).get.tricks shouldBe 2
      initialState.players.find(_.id == 0).get.predictedTricks shouldBe 2
      initialState.players.find(_.id == 0).get.totalPoints shouldBe 10
    }

    "return a new GameState instance" in {
      val result =
        ScoreRoundCommand.execute(initialState)

      result should not be theSameInstanceAs(initialState)
    }
  }
}
