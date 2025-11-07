package de.htwg.wizard

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class Wizardtest extends AnyWordSpec with Matchers {

  // getPlayerCount tests MAIN
  "getPlayerCount" when {
    "receives valid input between 3 and 6" should {
      "return the same number" in {
        getPlayerCount("3") should be(3)
        getPlayerCount("4") should be(4)
        getPlayerCount("5") should be(5)
        getPlayerCount("6") should be(6)
      }
    }

    "receives invalid input (below 3 or above 6)" should {
      "retry until valid input" in {
        var inputs = List("2", "7", "4")
        val result = getPlayerCount({
          val head = inputs.head
          inputs = inputs.tail
          head
        })
        result should be(4)
      }
    }

    "receives non-numeric input" should {
      "retry until valid input" in {
        var inputs = List("abc", "5")
        val result = getPlayerCount({
          val head = inputs.head
          inputs = inputs.tail
          head
        })
        result should be(5)
      }
    }
  }

  // stringBeginningRound tests MAIN
  "stringBeginningRound" when {
    "called with valid parameters" should {
      "return a formatted string" in {
        val result = stringBeginningRound(4, 1)

        result should include("There are 4 players")
        result should include("round: 1")
        result should include("Trump is: Card x,y")
      }
    }
  }

  // stringPlayerAndCards → round() test
  "round" when {
    "called with valid parameters" should {
      "return player strings for all players" in {

        val cardarray = Array(
          "blue 1", "blue 2", "blue 3",
          "green 1", "green 2", "green 3",
          "red 1", "red 2", "red 3",
          "yellow 1", "yellow 2", "yellow 3"
        )

        val result = round(3, 1, cardarray).mkString("\n")

        result should include("Player 0")
        result should include("Player 1")
        result should include("Player 2")
        result should include("Cards:")
      }
    }
  }

  // main function
  "main" should {
    "run without throwing an exception" in {
      noException should be thrownBy main()
    }
  }
}
