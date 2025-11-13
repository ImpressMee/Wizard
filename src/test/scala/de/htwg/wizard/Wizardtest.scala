package de.htwg.wizard

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable.Stack
import scala.io.StdIn.readLine

class Wizardtest extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // getPlayerCount
  // ------------------------------------------------------------
  "getPlayerCount" should {

    "return the same number for valid input" in {
      getPlayerCount("3") shouldBe 3
      getPlayerCount("4") shouldBe 4
      getPlayerCount("5") shouldBe 5
      getPlayerCount("6") shouldBe 6
    }

    "retry until valid number on invalid numeric input" in {
      var inputs = List("2", "7", "4")

      val result = getPlayerCount({
        val h = inputs.head
        inputs = inputs.tail
        h
      })

      result shouldBe 4
    }

    "retry until valid number on non-numeric input" in {
      var inputs = List("abc", "5")

      val result = getPlayerCount({
        val h = inputs.head
        inputs = inputs.tail
        h
      })

      result shouldBe 5
    }
  }


  // ------------------------------------------------------------
  // stringBeginningRound
  // ------------------------------------------------------------
  "stringBeginningRound" should {

    "return formatted text and a valid trump color" in {
      val (text, trump) = stringBeginningRound(4, 1)

      text should include("There are 4 players")
      text should include("round: 1")
      text should include("Trump is:")

      Array("red", "green", "blue", "yellow") should contain(trump)
    }
  }


  // ------------------------------------------------------------
  // shuffle
  // ------------------------------------------------------------
  "shuffle" should {

    "return a stack with the same elements but shuffled" in {
      val cards = Array("a", "b", "c", "d")
      val mixed = shuffle(cards)

      mixed.size shouldBe 4
      mixed.toSet shouldBe cards.toSet
    }
  }


  // ------------------------------------------------------------
  // dealcards
  // ------------------------------------------------------------
  "dealcards" should {

    "deal cards correctly to players" in {
      val cards = Stack("a", "b", "c", "d")
      val result = dealcards(2, 2, cards)

      result.length shouldBe 2
      result(0).length shouldBe 2
      result(1).length shouldBe 2

      cards shouldBe empty
    }
  }


  // ------------------------------------------------------------
  // stitchPointsCalculator
  // ------------------------------------------------------------
  "stitchPointsCalculator" should {

    "calculate correct positive and negative points" in {
      val prediction = Array(1, 2, 0)
      val stitches   = Array(1, 1, 3)

      val result = stitchPointsCalculator(prediction, stitches, 3)

      result(0) shouldBe 30
      result(1) shouldBe -10
      result(2) shouldBe -30
    }
  }


  // ------------------------------------------------------------
  // stitchPrediction (MANUAL INPUT)
  // ------------------------------------------------------------
  "stitchPrediction" should {

    "ask the user and read predictions (manual input)" in {
      println("\n--- TEST stitchPrediction ---")
      println("Bitte 3 Zahlen eingeben (z.B. 0 1 2):")

      val result = stitchPrediction(readLine(), 3)

      result.length shouldBe 3
    }
  }


  // ------------------------------------------------------------
  // stitchgame (MANUAL INPUT)
  // ------------------------------------------------------------
  "stitchgame" should {

    "ask for card indices (manual input)" in {
      println("\n--- TEST stitchgame ---")
      println("Bitte 3 Kartenindizes eingeben (z.B. 0 0 0):")

      val cards = Array(
        Array("blue 1"),
        Array("green 2"),
        Array("red 3")
      )

      val result = stitchgame(3, 1, cards, "blue", readLine())

      result.length shouldBe 3
    }
  }


  // ------------------------------------------------------------
  // round (MANUAL INPUT)
  // ------------------------------------------------------------
  "round" should {

    "run a full interactive round (manual input)" in {
      println("\n--- TEST round ---")
      println("Bitte Stiche eingeben + Karten auswählen.\n")

      val cardarray = Array(
        "blue 1", "blue 2", "blue 3",
        "green 1", "green 2", "green 3",
        "red 1", "red 2", "red 3",
        "yellow 1", "yellow 2", "yellow 3"
      )

      val result = round(3, 1, cardarray)

      result.length shouldBe 3
    }
  }


  // ------------------------------------------------------------
  // main (MANUAL INPUT)
  // ------------------------------------------------------------
  "main" should {

    "run the full game (manual input)" in {
      println("\n--- TEST main ---")
      println("Bitte Spieleranzahl + Stiche + Karten eingeben.")

      noException shouldBe thrownBy {
        main()
      }
    }
  }
}
