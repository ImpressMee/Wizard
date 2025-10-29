package de.htwg.wizard

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class Wizardtest extends AnyWordSpec with Matchers {

  "getPlayerCount" when {
    "receives valid input between 3 and 6" should {
      "return the same number" in {
        getPlayerCount("3") should be (3)
        getPlayerCount("4") should be (4)
        getPlayerCount("5") should be (5)
        getPlayerCount("6") should be (6)
      }
    }

    /**
     * getPlayerCount:
     *   - Nimmt erste Eingabe -> "abc"
     *   - Fehler -> ruft sich selbst wieder auf
     *   - Nimmt nächste Eingabe -> "4"
     *   - Passt -> return 4
     */
    "receives invalid input below 3 or above 6 or other it" should {
      "reject the value and retry" in {
        var inputs = List("abc", "4") // first invalid, then valid
        val result = getPlayerCount({
          val head = inputs.head
          inputs = inputs.tail
          head
        })
        result should be (4)
      }
    }
  }

  /**
   * TODO: print_tui Test_cases
   * TODO: stitch_prediction Test_cases
   */
}
