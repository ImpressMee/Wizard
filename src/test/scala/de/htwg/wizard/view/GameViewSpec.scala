package de.htwg.wizard.view

import de.htwg.wizard.model.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

class GameViewSpec extends AnyWordSpec with Matchers {

  def withInOut(input: String)(test: => Unit): String =
    val in = new ByteArrayInputStream(input.getBytes)
    val out = new ByteArrayOutputStream()
    Console.withIn(in) {
      Console.withOut(new PrintStream(out)) {
        test
      }
    }
    out.toString

  "GameView" should {

    // ----------------------------------------------------------
    // askPlayerAmount
    // ----------------------------------------------------------

    "print start message in askPlayerAmount" in {
      val view = new GameView()
      val out = withInOut("") {
        view.askPlayerAmount()
      }
      out should include ("Game Start")
      out should include ("How many Players")
    }

    // ----------------------------------------------------------
    // readPlayerAmount
    // ----------------------------------------------------------

    "read valid player amount" in {
      val view = new GameView()
      val res = withInOut("4\n") {
        view.readPlayerAmount() shouldBe 4
      }
      res shouldBe ""
    }

    "retry on invalid number format" in {
      val view = new GameView()
      val out = withInOut("abc\n5\n") {
        view.readPlayerAmount() shouldBe 5
      }
      out should include ("Please enter a valid number!")
    }

    "retry when number is out of allowed range" in {
      val view = new GameView()
      val out = withInOut("10\n3\n") {
        view.readPlayerAmount() shouldBe 3
      }
      out should include ("Wrong amount! Try again.")
    }

    // ----------------------------------------------------------
    // readPositiveInt
    // ----------------------------------------------------------

    "read valid positive integer" in {
      val view = new GameView()
      val out = withInOut("7\n") {
        view.readPositiveInt() shouldBe 7
      }
      out shouldBe ""
    }

    "retry on negative integer" in {
      val view = new GameView()
      val out = withInOut("-3\n2\n") {
        view.readPositiveInt() shouldBe 2
      }
      out should include ("Index out of range! Try again.")
    }

    "retry on invalid integer input" in {
      val view = new GameView()
      val out = withInOut("xyz\n1\n") {
        view.readPositiveInt() shouldBe 1
      }
      out should include ("Please enter a valid number!")
    }

    // ----------------------------------------------------------
    // askPlayerCard / readIndex
    // ----------------------------------------------------------

    "read valid card index" in {
      val view = new GameView()
      val p = Player(0, hand = List(Card(CardColor.Red, 3)))

      val out = withInOut("1\n") {
        view.askPlayerCard(p)
        view.readIndex(p) shouldBe 0
      }

      out should include ("Which card do you wanna play")
    }

    "retry on invalid index" in {
      val view = new GameView()
      val p = Player(0, hand = List(Card(CardColor.Blue, 1)))

      val out = withInOut("5\n1\n") {
        view.readIndex(p) shouldBe 0
      }
      out should include ("Index out of range!")
    }

    "retry on invalid index input format" in {
      val view = new GameView()
      val p = Player(0, hand = List(Card(CardColor.Green, 9)))

      val out = withInOut("abc\n1\n") {
        view.readIndex(p) shouldBe 0
      }
      out should include ("Please enter a valid number!")
    }

    // ----------------------------------------------------------
    // showError / update
    // ----------------------------------------------------------

    "print error message" in {
      val view = new GameView()
      val out = withInOut("") {
        view.showError("Fehler!")
      }
      out should include ("Fehler!")
    }

    "print update notification" in {
      val view = new GameView()
      val out = withInOut("") {
        view.update()
      }
      out should include ("update display")
    }

    // ----------------------------------------------------------
    // colorize
    // ----------------------------------------------------------

    "colorize red cards correctly" in {
      val view = new GameView
      view.colorize(Card(CardColor.Red, 2)) should include("Card(Red,2)")
    }

    "colorize blue cards correctly" in {
      val view = new GameView
      view.colorize(Card(CardColor.Blue, 3)) should include("Card(Blue,3)")
    }

    "colorize green cards correctly" in {
      val view = new GameView
      view.colorize(Card(CardColor.Green, 1)) should include("Card(Green,1)")
    }

    "colorize yellow cards correctly" in {
      val view = new GameView
      view.colorize(Card(CardColor.Yellow, 2)) should include("Card(Yellow,2)")
    }

    // ----------------------------------------------------------
    // showRoundInfo
    // ----------------------------------------------------------

    "showRoundInfo should print correct info" in {
      val view = new GameView()
      val out = withInOut("") {
        view.showRoundInfo(2, CardColor.Red, 3)
      }
      out should include ("Round 2")
      out should include ("Trump is: Red")
      out should include ("There are 3 players.")
    }

    // ----------------------------------------------------------
    // showPlayerCards
    // ----------------------------------------------------------

    "showPlayerCards prints player hand" in {
      val view = new GameView()
      val p = Player(0, hand = List(Card(CardColor.Red, 3)))

      val out = withInOut("") {
        view.showPlayerCards(p)
      }
      out should include ("Player0")
      out should include ("Red")
    }

    // ----------------------------------------------------------
    // showHowManyTricks
    // ----------------------------------------------------------

    "askHowManyTricks prints question" in {
      val view = new GameView()
      val p = Player(0, List(Card(CardColor.Blue, 2)))

      val out = withInOut("") {
        view.askHowManyTricks(p)
      }
      out should include ("How many tricks will you make player0")
    }

    // ----------------------------------------------------------
    // showTrickStart
    // ----------------------------------------------------------

    "showTrickStart prints correct message" in {
      val view = new GameView()
      val out = withInOut("") {
        view.showTrickStart(3)
      }
      out should include ("Trick 3 start")
    }

    // ----------------------------------------------------------
    // showTrickWinner
    // ----------------------------------------------------------

    "showTrickWinner prints winner" in {
      val view = new GameView()
      val p = Player(2)

      val out = withInOut("") {
        view.showTrickWinner(p, Card(CardColor.Yellow, 4))
      }

      out should include ("Player2 won this trick")
    }

    // ----------------------------------------------------------
    // showRoundEvaluation
    // ----------------------------------------------------------

    "showRoundEvaluation prints player stats" in {
      val view = new GameView()

      val players = List(
        Player(0, tricks = 1, predictedTricks = 1, totalPoints = 30),
        Player(1, tricks = 0, predictedTricks = 2, totalPoints = 10)
      )

      val out = withInOut("") {
        view.showRoundEvaluation(2, players)
      }

      out should include ("Round 2")
      out should include ("Player 0")
      out should include ("Player 1")
    }

    // ----------------------------------------------------------
    // showGameWinner
    // ----------------------------------------------------------

    "showGameWinner prints final winner" in {
      val view = new GameView()
      val p = Player(3, totalPoints = 99)

      val out = withInOut("") {
        view.showGameWinner(p)
      }

      out should include ("Player3")
      out should include ("99")
    }

  }
}
