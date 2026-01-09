package de.htwg.wizard.control

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*

class GameStatePhaseSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Hilfs-GameStates
  // ---------------------------------------------------------

  val emptyState =
    GameState(
      amountOfPlayers = 2,
      players = List(Player(0), Player(1)),
      deck = Deck(),
      currentRound = 0,
      totalRounds = 3,
      currentTrump = None,
      currentTrick = None,
      completedTricks = 0
    )

  val stateWithCards =
    emptyState.copy(
      players = List(
        Player(0, hand = List(Card(CardColor.Red, 5))),
        Player(1, hand = List(Card(CardColor.Blue, 7)))
      )
    )

  val stateWithoutCards =
    emptyState.copy(
      players = List(
        Player(0, hand = Nil),
        Player(1, hand = Nil)
      )
    )

  // ---------------------------------------------------------
  // INIT
  // ---------------------------------------------------------

  "InitState" should {
    "always transition to PredictState" in {
      InitState.next(emptyState) shouldBe PredictState
    }
  }

  // ---------------------------------------------------------
  // PREPARE
  // ---------------------------------------------------------

  "PrepareRoundState" should {
    "transition to PredictState" in {
      PrepareRoundState.next(emptyState) shouldBe PredictState
    }
  }

  // ---------------------------------------------------------
  // PREDICT
  // ---------------------------------------------------------

  "PredictState" should {
    "start with TrickState(1)" in {
      PredictState.next(emptyState) shouldBe TrickState(1)
    }
  }

  // ---------------------------------------------------------
  // TRICK STATE
  // ---------------------------------------------------------

  "TrickState" should {

    "advance to next TrickState if players still have cards" in {
      val phase = TrickState(1)
      phase.next(stateWithCards) shouldBe TrickState(2)
    }

    "transition to ScoreState if no player has cards left" in {
      val phase = TrickState(3)
      phase.next(stateWithoutCards) shouldBe ScoreState
    }
  }

  // ---------------------------------------------------------
  // SCORE
  // ---------------------------------------------------------

  "ScoreState" should {

    "transition to PrepareRoundState if more rounds are left" in {
      val midGame =
        emptyState.copy(currentRound = 1, totalRounds = 3)

      ScoreState.next(midGame) shouldBe PrepareRoundState
    }

    "transition to FinishState if last round is reached" in {
      val lastRound =
        emptyState.copy(currentRound = 3, totalRounds = 3)

      ScoreState.next(lastRound) shouldBe FinishState
    }
  }

  // ---------------------------------------------------------
  // FINISH
  // ---------------------------------------------------------

  "FinishState" should {
    "remain in FinishState forever" in {
      FinishState.next(emptyState) shouldBe FinishState
    }
  }
}
