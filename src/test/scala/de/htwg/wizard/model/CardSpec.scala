package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CardModelSpec extends AnyWordSpec with Matchers {

  "CardColor enum" should {
    "contain exactly the four colors" in {
      CardColor.values.toSet shouldBe
        Set(CardColor.Red, CardColor.Green, CardColor.Blue, CardColor.Yellow)
    }
  }

  "Card" should {
    "store color and value correctly" in {
      val card = Card(CardColor.Red, 5)
      card.color shouldBe CardColor.Red
      card.value shouldBe 5
    }
  }

  "trumpColor()" should {
    "always return a valid CardColor" in {
      val validColors = CardColor.values.toSet

      for _ <- 1 to 1000 do
        validColors should contain(trumpColor())
      // never outside enum
    }

    "return different values statistically (randomness test)" in {
      val samples = (1 to 200).map(_ => trumpColor()).toSet
      samples.size should be > 1   // should not be always same
    }
  }
}
