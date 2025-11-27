package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TrickSpec extends AnyWordSpec with Matchers {

  "A Trick" should {

    "store the played map exactly as provided" in {
      val played = Map(
        1 -> Card(CardColor.Red, 2),
        2 -> Card(CardColor.Blue, 1)
      )

      val trick = Trick(played)
      trick.played shouldBe played
    }

    "allow an empty played map" in {
      val trick = Trick(Map.empty)
      trick.played shouldBe empty
    }

    "contain the correct number of entries" in {
      val played = Map(
        1 -> Card(CardColor.Green, 3),
        2 -> Card(CardColor.Yellow, 1),
        3 -> Card(CardColor.Red, 2)
      )

      val trick = Trick(played)
      trick.played.size shouldBe 3
    }

    "allow lookup of cards by PlayerID" in {
      val played = Map(
        5 -> Card(CardColor.Blue, 3)
      )
      val trick = Trick(played)

      trick.played(5) shouldBe Card(CardColor.Blue, 3)
    }

    "fail lookup for a non-existing PlayerID" in {
      val trick = Trick(Map(1 -> Card(CardColor.Red, 1)))

      //assertThrows[NoSuchElementException] { trick.played(99) } checks:
      //that executing the code inside the block
      //trick.played(99)
      //throws exactly a NoSuchElementException
      assertThrows[NoSuchElementException] {
        trick.played(99)
      }
    }

    "support immutability when copying (adding entries)" in {
      val trick = Trick(Map(1 -> Card(CardColor.Red, 1)))
      val updated = trick.copy(played = trick.played + (2 -> Card(CardColor.Green, 3)))

      updated.played.size shouldBe 2
      updated.played(2) shouldBe Card(CardColor.Green, 3)
      trick.played.size shouldBe 1
    }

    "be equal when maps are equal" in {
      val m = Map(1 -> Card(CardColor.Red, 1))
      Trick(m) shouldBe Trick(m)
    }

    "not be equal when maps differ" in {
      Trick(Map(1 -> Card(CardColor.Red, 1))) should not be
        Trick(Map(2 -> Card(CardColor.Red, 1)))
    }
  }
}
