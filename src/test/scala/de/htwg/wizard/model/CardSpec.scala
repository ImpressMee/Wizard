package de.htwg.wizard.model

import de.htwg.wizard.model.modelComponent.{Card, CardColor, CardType, JokerCard, NormalCard, WizardCard, determinesColor, isJoker, isNormal, isWizard, trumpColor, value}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CardSpec extends AnyWordSpec with Matchers {

  "CardType enum" should {

    "contain all expected cases" in {
      CardType.Normal(5) shouldBe CardType.Normal(5)
      CardType.Wizard shouldBe CardType.Wizard
      CardType.Joker shouldBe CardType.Joker
    }
  }

  "CardColor enum" should {

    "contain all colors" in {
      CardColor.values.toSet shouldBe Set(
        CardColor.Red,
        CardColor.Green,
        CardColor.Blue,
        CardColor.Yellow
      )
    }
  }

  "NormalCard" should {

    "have correct color, value and type" in {
      val c = NormalCard(CardColor.Red, 7)

      c.color shouldBe CardColor.Red
      c.cardType shouldBe CardType.Normal(7)
      de.htwg.wizard.model.value(c) shouldBe 7
      isNormal(c) shouldBe true
      isWizard(c) shouldBe false
      isJoker(c) shouldBe false
      determinesColor(c) shouldBe true
    }
  }

  "WizardCard" should {

    "have correct color and type" in {
      val c = WizardCard(CardColor.Blue)

      c.color shouldBe CardColor.Blue
      c.cardType shouldBe CardType.Wizard
      modelComponent.value(c) shouldBe 0
      isWizard(c) shouldBe true
      isNormal(c) shouldBe false
      isJoker(c) shouldBe false
      determinesColor(c) shouldBe false
    }
  }

  "JokerCard" should {

    "have correct color and type" in {
      val c = JokerCard(CardColor.Green)

      c.color shouldBe CardColor.Green
      c.cardType shouldBe CardType.Joker
      modelComponent.value(c) shouldBe 0
      isJoker(c) shouldBe true
      isNormal(c) shouldBe false
      isWizard(c) shouldBe false
      determinesColor(c) shouldBe false
    }
  }

  "Helper functions" should {

    "detect card types correctly" in {
      val n = NormalCard(CardColor.Yellow, 3)
      val w = WizardCard(CardColor.Red)
      val j = JokerCard(CardColor.Blue)

      isNormal(n) shouldBe true
      isNormal(w) shouldBe false
      isNormal(j) shouldBe false

      isWizard(w) shouldBe true
      isWizard(n) shouldBe false

      isJoker(j) shouldBe true
      isJoker(n) shouldBe false
    }

    "return a valid trump color" in {
      CardColor.values.contains(trumpColor()) shouldBe true
    }
  }

  "Card factory" should {

    "create a NormalCard from color and value" in {
      val c = Card(CardColor.Red, 10)

      c shouldBe NormalCard(CardColor.Red, 10)
      isNormal(c) shouldBe true
    }

    "create a WizardCard from string" in {
      val c = Card(CardColor.Blue, "wizard")

      c shouldBe WizardCard(CardColor.Blue)
      isWizard(c) shouldBe true
    }

    "create a JokerCard from string" in {
      val c = Card(CardColor.Green, "joker")

      c shouldBe JokerCard(CardColor.Green)
      isJoker(c) shouldBe true
    }

    "throw an exception for an unknown card type" in {
      an[IllegalArgumentException] should be thrownBy {
        Card(CardColor.Red, "dragon")
      }
    }
  }
}
