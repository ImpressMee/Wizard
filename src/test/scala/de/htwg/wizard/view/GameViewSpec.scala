package de.htwg.wizard.view

import de.htwg.wizard.model.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class GameViewSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------------
  // Helper: Capture console output
  // ------------------------------------------------------------------
  def captureOutput(testCode: => Unit): String = {
    val out = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(out)) {
      testCode
    }
    out.toString
  }

  // ------------------------------------------------------------------
  // Helper: simulate input for readLine()
  // ------------------------------------------------------------------
  def withInput(str: String)(testCode: => Unit): Unit = {
    val in = new ByteArrayInputStream(str.getBytes())
    Console.withIn(in) {
      testCode
    }
  }

  val view = new GameView

  val red5   = NormalCard(CardColor.Red, 5)
  val wizG   = WizardCard(CardColor.Green)
  val jokY   = JokerCard(CardColor.Yellow)

  // ------------------------------------------------------------------
  // writeOneCard()
  // ------------------------------------------------------------------
  "writeOneCard" should {
    "format normal card" in {
      view.writeOneCard(red5) shouldBe "Red 5"
    }

    "format wizard card" in {
      view.writeOneCard(wizG) shouldBe "Green WIZARD"
    }

    "format joker card" in {
      view.writeOneCard(jokY) shouldBe "Yellow JOKER"
    }
  }

  // ------------------------------------------------------------------
  // chooseTrump()
  // ------------------------------------------------------------------
  "chooseTrump" should {

    "return correct color for input 1-4" in {
      withInput("1\n") {
        view.chooseTrump() shouldBe CardColor.Red
      }
      withInput("2\n") {
        view.chooseTrump() shouldBe CardColor.Green
      }
      withInput("3\n") {
        view.chooseTrump() shouldBe CardColor.Blue
      }
      withInput("4\n") {
        view.chooseTrump() shouldBe CardColor.Yellow
      }
    }

    "retry on invalid input" in {
      withInput("X\n1\n") {
        view.chooseTrump() shouldBe CardColor.Red
      }
    }
  }

  // ------------------------------------------------------------------
  // readPlayerAmount()
  // ------------------------------------------------------------------
  "readPlayerAmount" should {

    "accept valid number" in {
      withInput("4\n") {
        view.readPlayerAmount() shouldBe 4
      }
    }

    "reject invalid number and retry" in {
      withInput("9\n3\n") {
        view.readPlayerAmount() shouldBe 3
      }
    }

    "reject non-number and retry" in {
      withInput("abc\n3\n") {
        view.readPlayerAmount() shouldBe 3
      }
    }
  }

  // ------------------------------------------------------------------
  // readPositiveInt()
  // ------------------------------------------------------------------
  "readPositiveInt" should {
    "accept valid integer" in {
      withInput("5\n") {
        view.readPositiveInt() shouldBe 5
      }
    }

    "retry on invalid input" in {
      withInput("-3\n2\n") {
        view.readPositiveInt() shouldBe 2
      }
    }
  }

  // ------------------------------------------------------------------
  // readIndex()
  // ------------------------------------------------------------------
  "readIndex" should {
    val p = Player(0, hand = List(red5, wizG, jokY))

    "return correct index" in {
      withInput("2\n") {
        view.readIndex(p) shouldBe 1
      }
    }

    "retry on invalid index" in {
      withInput("9\n1\n") {
        view.readIndex(p) shouldBe 0
      }
    }

    "retry on invalid input" in {
      withInput("X\n3\n") {
        view.readIndex(p) shouldBe 2
      }
    }
  }

  // ------------------------------------------------------------------
  // Formatting / Output tests
  // ------------------------------------------------------------------
  "showRoundInfo" should {
    "print correct text" in {
      val text = captureOutput {
        view.showRoundInfo(2, Some(CardColor.Blue), 4)
      }

      text should include("Round 2 start")
      text should include("Trump color is: Blue")
      text should include("There are 4 players.")
    }

    "print no-trump message" in {
      val text = captureOutput {
        view.showRoundInfo(1, None, 3)
      }

      text should include("there is no trump")
    }
  }

  "showPlayerCards" should {
    "print player's cards" in {
      val p = Player(1, List(red5, wizG))

      val text = captureOutput {
        view.showPlayerCards(p)
      }

      text should include("Player1")
      text should include("Red 5")
      text should include("Green WIZARD")
    }
  }

  "askHowManyTricks" should {
    "print correct prompt" in {
      val p = Player(0, List(red5))

      val text = captureOutput {
        view.askHowManyTricks(p)
      }

      text should include("How many tricks will you make player0?")
    }
  }

  "showTrickStart" should {
    "print correct header" in {
      val text = captureOutput {
        view.showTrickStart(3)
      }

      text should include("Trick 3 start")
    }
  }

  "askPlayerCard" should {
    "print card prompt" in {
      val p = Player(2, List(red5))

      val text = captureOutput {
        view.askPlayerCard(p)
      }

      text should include("Which card do you wanna play Player2?")
    }
  }

  "showTrickWinner" should {
    "print correct winner message" in {
      val p = Player(1)
      val text = captureOutput {
        view.showTrickWinner(p, red5)
      }
      text should include("Player1 won this trick")
    }
  }

  "showRoundEvaluation" should {
    "print scores" in {
      val p = List(Player(0, tricks = 1, predictedTricks = 2, totalPoints = 10))

      val text = captureOutput {
        view.showRoundEvaluation(1, p)
      }

      text should include("Player 0")
      text should include("actual tricks:    1")
      text should include("points in total")
    }
  }

  "showGameWinner" should {
    "print correct winner message" in {
      val p = Player(1, totalPoints = 77)

      val text = captureOutput {
        view.showGameWinner(p)
      }

      text should include("Game Winner")
      text should include("Player1")
      text should include("77 points")
    }
  }

  "showError" should {
    "print error message" in {
      val text = captureOutput {
        view.showError("Fehler")
      }

      text should include("Fehler")
    }
  }

}
