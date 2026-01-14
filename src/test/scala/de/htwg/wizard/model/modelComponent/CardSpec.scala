package de.htwg.wizard.model.modelComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CardSpec extends AnyWordSpec with Matchers {

  "CardType" should {
    "store value for Normal cards" in {
      val ct = CardType.Normal(7)
      ct match
        case CardType.Normal(v) => v shouldBe 7
        case _ => fail("Expected Normal card")
    }

    "distinguish Wizard and Joker" in {
      CardType.Wizard shouldBe CardType.Wizard
      CardType.Joker  shouldBe CardType.Joker
    }
  }

  "CardColor" should {
    "contain all four colors" in {
      CardColor.values.toSet shouldBe
        Set(CardColor.Red, CardColor.Green, CardColor.Blue, CardColor.Yellow)
    }
  }

  "NormalCard" should {
    "have color, value and Normal cardType" in {
      val c = NormalCard(CardColor.Red, 5)

      c.color shouldBe CardColor.Red
      c.value shouldBe 5
      c.cardType shouldBe CardType.Normal(5)
    }
  }

  "WizardCard" should {
    "have Wizard cardType and value 0" in {
      val c = WizardCard(CardColor.Blue)

      c.color shouldBe CardColor.Blue
      c.cardType shouldBe CardType.Wizard
    }
  }

  "JokerCard" should {
    "have Joker cardType and value 0" in {
      val c = JokerCard(CardColor.Green)

      c.color shouldBe CardColor.Green
      c.cardType shouldBe CardType.Joker
    }
  }

  "Helper functions" should {

    "detect wizard cards" in {
      isWizard(WizardCard(CardColor.Red)) shouldBe true
      isWizard(JokerCard(CardColor.Red))  shouldBe false
    }

    "detect joker cards" in {
      isJoker(JokerCard(CardColor.Blue))  shouldBe true
      isJoker(WizardCard(CardColor.Blue)) shouldBe false
    }

    "detect normal cards" in {
      isNormal(NormalCard(CardColor.Green, 3)) shouldBe true
      isNormal(JokerCard(CardColor.Green))     shouldBe false
    }

    "determine color only for normal cards" in {
      determinesColor(NormalCard(CardColor.Red, 1)) shouldBe true
      determinesColor(WizardCard(CardColor.Red))    shouldBe false
    }

    "produce a valid trump color" in {
      CardColor.values.contains(trumpColor()) shouldBe true
    }
  }

  "Card factory" should {

    "create normal cards via value constructor" in {
      val c = Card(CardColor.Red, 10)

      c shouldBe a[NormalCard]
      c.color shouldBe CardColor.Red
    }

    "create wizard cards via string constructor" in {
      val c = Card(CardColor.Blue, "wizard")

      c shouldBe a[WizardCard]
      isWizard(c) shouldBe true
    }

    "create joker cards via string constructor" in {
      val c = Card(CardColor.Green, "joker")

      c shouldBe a[JokerCard]
      isJoker(c) shouldBe true
    }

    "throw exception on unknown card type" in {
      an[IllegalArgumentException] shouldBe thrownBy {
        Card(CardColor.Red, "unknown")
      }
    }
  }
}
