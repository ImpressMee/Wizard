package de.htwg.wizard.control.controlComponent

import de.htwg.wizard.control.controlComponent.{FinishState, InitState, PredictState, ScoreState, TrickState}
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameStatePhaseSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Helper GameStates
  // ---------------------------------------------------------
  val emptyState =
    GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = Deck(),
      currentRound = 0,
      totalRounds = 0
    )

  val predictedState =
    GameState(
      amountOfPlayers = 2,
      players = List(
        Player(0, predictedTricks = 1),
        Player(1, predictedTricks = 0)
      ),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  val notPredictedState =
    GameState(
      amountOfPlayers = 2,
      players = List(
        Player(0, predictedTricks = -1),
        Player(1, predictedTricks = 0)
      ),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  val emptyHandState =
    GameState(
      amountOfPlayers = 2,
      players = List(
        Player(0, hand = Nil),
        Player(1, hand = Nil)
      ),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  val nonEmptyHandState =
    GameState(
      amountOfPlayers = 2,
      players = List(
        Player(0, hand = List(Card(CardColor.Red, 1))),
        Player(1, hand = List(Card(CardColor.Blue, 2)))
      ),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "InitState" should {
    "always stay in InitState" in {
      InitState.next(emptyState) shouldBe InitState
    }
  }

  "PredictState" should {

    "transition to TrickState(1) when all players have valid predictions" in {
      PredictState.next(predictedState) shouldBe TrickState(1)
    }

    "stay in PredictState if at least one prediction is missing or invalid" in {
      PredictState.next(notPredictedState) shouldBe PredictState
    }
  }

  "TrickState" should {

    "transition to ScoreState when all players have empty hands" in {
      TrickState(1).next(emptyHandState) shouldBe ScoreState
    }

    "stay in TrickState while players still have cards" in {
      TrickState(2).next(nonEmptyHandState) shouldBe TrickState(2)
    }
  }

  "ScoreState" should {
    "block and stay in ScoreState" in {
      ScoreState.next(predictedState) shouldBe ScoreState
    }
  }

  "FinishState" should {
    "block and stay in FinishState" in {
      FinishState.next(predictedState) shouldBe FinishState
    }
  }
}
