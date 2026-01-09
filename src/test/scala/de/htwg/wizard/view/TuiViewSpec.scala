package de.htwg.wizard.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.event.*
import de.htwg.wizard.model.*

class TuiViewSpec extends AnyWordSpec with Matchers {

  private def captureStdOut(block: => Unit): String = {
    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(out) {
      block
    }
    out.toString
  }

  "TuiView" should {

    "print a game start message on PlayerAmountRequested" in {
      val tui = new TuiView
      val state = GameState(0, Nil, Deck(), 0, 0)

      val output = captureStdOut {
        tui.update(PlayerAmountRequested(state))
      }

      output should include ("Game Start")
      output should include ("How many players")
    }

    "print prediction phase information on PredictionsRequested" in {
      val tui = new TuiView
      val players = List(Player(0), Player(1))
      val state = GameState(2, players, Deck(), 1, 5)

      val output = captureStdOut {
        tui.update(PredictionsRequested(state))
      }

      output should include ("Prediction Phase")
      output should include ("Player 0")
      output should include ("Player 1")
    }

    "print trick start information on TrickMoveRequested" in {
      val tui = new TuiView
      val players =
        List(
          Player(0, hand = List(NormalCard(CardColor.Red, 5))),
          Player(1, hand = List(NormalCard(CardColor.Blue, 7)))
        )
      val state = GameState(2, players, Deck(), 1, 5)

      val output = captureStdOut {
        tui.update(TrickMoveRequested(1, state))
      }

      output should include ("Trick 1")
      output should include ("Which card do you want to play")
    }

    "print round evaluation on RoundFinished" in {
      val tui = new TuiView
      val players =
        List(
          Player(0, tricks = 1, predictedTricks = 1, totalPoints = 30),
          Player(1, tricks = 0, predictedTricks = 1, totalPoints = -10)
        )
      val state = GameState(2, players, Deck(), 1, 5)

      val output = captureStdOut {
        tui.update(RoundFinished(state))
      }

      output should include ("Round Evaluation")
      output should include ("Player 0")
      output should include ("total points")
    }

    "print winner information on GameFinished" in {
      val tui = new TuiView
      val winner = Player(1, totalPoints = 50)
      val state = GameState(2, List(winner), Deck(), 5, 5)

      val output = captureStdOut {
        tui.update(GameFinished(winner, state))
      }

      output should include ("Game Winner")
      output should include ("Player 1")
      output should include ("50")
    }
  }
}
