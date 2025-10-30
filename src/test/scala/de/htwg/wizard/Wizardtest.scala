package de.htwg.wizard

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class Wizardtest extends AnyWordSpec with Matchers {
  //getPlayerCount tests MAIN
  "getPlayerCount" when {
    "receives valid input between 3 and 6" should {
      "return the same number" in {
        getPlayerCount("3") should be(3)
        getPlayerCount("4") should be(4)
        getPlayerCount("5") should be(5)
        getPlayerCount("6") should be(6)
      }
    }

    /**
     * getPlayerCount:
     *   - Nimmt erste Eingabe -> "abc"
     *   - Fehler -> ruft sich selbst wieder auf
     *   - Nimmt nächste Eingabe -> "4"
     *   - Passt -> return 4
     */
    "getPlayerCount receives invalid input below 3 or above 6 or other it" should {
      "reject the value and retry" in {
        var inputs = List("abc", "4") // first invalid, then valid
        val result = getPlayerCount({
          val head = inputs.head
          inputs = inputs.tail
          head
        })
        result should be(4)
      }
    }
  }

  //stringBeginningRound tests MAIN
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
    //stringPlayerAndCards tests MAIN
    "stringPlayerAndCards" when {
      "called with valid parameters (vorher geprüft)" should {
        "return a formatted string containing player count, round, and trump" in {
          val result = stringPlayerAndCards(3, 1).mkString("\n")

          val expected =
            """+-----------+
              || Player 1|
              |+-----------+
              || Cards: -
              |+-----------+


              |+-----------+
              || Player 2|
              |+-----------+
              || Cards: -
              |+-----------+


              |+-----------+
              || Player 3|
              |+-----------+
              || Cards: -
              |+-----------+""".stripMargin

          result should include(expected.trim)
        }
      }
    }

    //stitch_prediction tests MAIN
    "stitch_prediction" when {
      "Player enters valid prediction" should {
        "read Number and return the same number and strings" in {
          val result = stitch_prediction(number_of_players = 3, round = 2)
          result shouldBe()
        }
      }
    }
}