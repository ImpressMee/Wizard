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
        var inputs = List("2", "7", "4") // first two invalid, last one valid
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
        var inputs = List("abc", "5") // invalid, then valid
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
    "called with valid parameters (vorher geprüft)" should {
      "return a formatted string containing player count, round, and trump" in {
        val result = stringBeginningRound(4, 1)

        result should include("There are 4 players")
        result should include("round: 1")
        result should include("Trump is: Card x,y")
      }
    }
  }

  // stringPlayerAndCards tests MAIN
  "stringPlayerAndCards" when {
    "called with valid parameters (vorher geprüft)" should {
      "return strings for all players" in {
        val result = stringPlayerAndCards(3, 1).mkString("\n")

        result should include("Player 1")
        result should include("Cards: -")
        result should include("Player 2")
        result should include("Cards: -")
        result should include("Player 3")
        result should include("Cards: -")
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
