package de.htwg.wizard.view

import de.htwg.wizard.control.{GameFinished, PlayerAmountRequested, PredictionsRequested, RoundFinished, RoundStarted, StateChanged, TrickFinished, TrickMoveRequested}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Deck, GameState, Player}

import java.io.{ByteArrayOutputStream, PrintStream}

class TuiViewSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Helper: capture console output
  // ---------------------------------------------------------
  def captureOut(block: => Unit): String = {
    val baos = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(baos)) {
      block
    }
    baos.toString
  }

  val baseState =
    GameState(
      amountOfPlayers = 2,
      players = List(Player(0), Player(1)),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "TuiView" should {

    "print game start message on PlayerAmountRequested" in {
      val tui = new TuiView

      val out = captureOut {
        tui.update(PlayerAmountRequested(baseState))
      }

      out should include ("Game Start")
      out should include ("How many players")
    }

    "print round start information on RoundStarted" in {
      val tui = new TuiView

      val out = captureOut {
        tui.update(RoundStarted(1, baseState))
      }

      out should include ("Round 1 start")
      out should include ("There are 2 players")
    }

    "print prediction phase output on PredictionsRequested" in {
      val tui = new TuiView

      val state =
        baseState.copy(
          players = List(
            Player(0, hand = List(Card(CardColor.Red, 1))),
            Player(1, hand = List(Card(CardColor.Blue, 2)))
          )
        )

      val out = captureOut {
        tui.update(PredictionsRequested(state))
      }

      out should include ("Prediction Phase")
      out should include ("Player 0")
      out should include ("Player 1")
      out should include ("How many tricks will you make")
    }

    "print trick start output on TrickMoveRequested" in {
      val tui = new TuiView

      val state =
        baseState.copy(
          players = List(
            Player(0, hand = List(Card(CardColor.Red, 1))),
            Player(1, hand = List(Card(CardColor.Blue, 2)))
          )
        )

      val out = captureOut {
        tui.update(TrickMoveRequested(1, state))
      }

      out should include ("Trick 1 start")
      out should include ("Which card do you want to play")
    }

    "print trick winner on TrickFinished" in {
      val tui = new TuiView

      val out = captureOut {
        tui.update(TrickFinished(0, baseState))
      }

      out should include ("Trick won by Player 0")
    }

    "print round evaluation on RoundFinished" in {
      val tui = new TuiView

      val state =
        baseState.copy(
          players = List(
            Player(0, tricks = 1, predictedTricks = 1, totalPoints = 20),
            Player(1, tricks = 0, predictedTricks = 1, totalPoints = -10)
          )
        )

      val out = captureOut {
        tui.update(RoundFinished(state))
      }

      out should include ("Round Evaluation")
      out should include ("Player 0")
      out should include ("total points")
    }

    "print winner information on GameFinished" in {
      val tui = new TuiView

      val winner = Player(0, totalPoints = 42)

      val out = captureOut {
        tui.update(GameFinished(winner, baseState))
      }

      out should include ("Game Winner")
      out should include ("Player 0")
      out should include ("42")
    }

    "ignore unrelated events safely" in {
      val tui = new TuiView

      noException shouldBe thrownBy {
        tui.update(StateChanged(baseState))
      }
    }

    "print green card with green console color" in {
      val tui = new TuiView

      val state =
        baseState.copy(
          players = List(
            Player(0, hand = List(Card(CardColor.Green, 5))),
            Player(1, hand = List(Card(CardColor.Red, 3)))
          )
        )

      val out = captureOut {
        tui.update(PredictionsRequested(state))
      }

      out should include(Console.GREEN)
    }

    "print yellow card with yellow console color" in {
      val tui = new TuiView

      val state =
        baseState.copy(
          players = List(
            Player(0, hand = List(Card(CardColor.Yellow, 7))),
            Player(1, hand = List(Card(CardColor.Blue, 2)))
          )
        )

      val out = captureOut {
        tui.update(PredictionsRequested(state))
      }

      out should include(Console.YELLOW)
    }

    "print trump color on RoundStarted when trump is defined" in {
      val tui = new TuiView

      val stateWithTrump =
        baseState.copy(
          currentTrump = Some(CardColor.Green)
        )

      val out = captureOut {
        tui.update(RoundStarted(1, stateWithTrump))
      }

      out should include("Trump color is:")
      out should include("Green")
    }

  }
}
