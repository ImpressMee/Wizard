package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*

class CardModelTest extends AnyWordSpec {

  "Card" should {
    "be created with a color and value" in {
      val card = Card(CardColor.Red, 2)
      card.color shouldBe CardColor.Red
      card.value shouldBe 2
    }


    "CardColor" should {
      "contain all 4 defined colors" in {
        CardColor.values.toSet should contain allOf(
          CardColor.Red,
          CardColor.Green,
          CardColor.Blue,
          CardColor.Yellow
        )
      }
    }

    "trumpColor" should {
      "return a valid CardColor from enum" in {
        val result = trumpColor()
        CardColor.values should contain(result)
      }

      "return varying results (not always same)" in {
        val colors = (1 to 100).map(_ => trumpColor()).toSet
        colors.size should be > 1
      }
    }
  }
}