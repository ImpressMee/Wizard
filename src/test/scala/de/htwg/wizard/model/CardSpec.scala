package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CardSpec extends AnyWordSpec with Matchers {

  "CardColor" should {
    "contain exactly Red, Green, Blue, Yellow" in {
      CardColor.values.toSet shouldBe
        Set(CardColor.Red, CardColor.Green, CardColor.Blue, CardColor.Yellow)
    }
  }

  "CardType" should {
    "store Normal(value) correctly" in {
      CardType.Normal(7).value shouldBe 7
    }

    "contain Wizard and Joker" in {
      CardType.Wizard shouldBe CardType.Wizard
      CardType.Joker  shouldBe CardType.Joker
    }
  }

  "NormalCard" should {
    "store its color and value correctly" in {
      val c = NormalCard(CardColor.Red, 5)
      c.color shouldBe CardColor.Red
      c.cardType shouldBe CardType.Normal(5)

      isNormal(c) shouldBe true
      isWizard(c) shouldBe false
      isJoker(c) shouldBe false

      de.htwg.wizard.model.value(c) shouldBe 5
    }
  }

  "WizardCard" should {
    "store color and be recognized as Wizard" in {
      val c = WizardCard(CardColor.Green)
      c.color shouldBe CardColor.Green
      c.cardType shouldBe CardType.Wizard

      isWizard(c) shouldBe true
      isNormal(c) shouldBe false
      isJoker(c) shouldBe false

      de.htwg.wizard.model.value(c) shouldBe 0
    }
  }

  "JokerCard" should {
    "store color and be recognized as Joker" in {
      val c = JokerCard(CardColor.Blue)
      c.color shouldBe CardColor.Blue
      c.cardType shouldBe CardType.Joker

      isJoker(c) shouldBe true
      isNormal(c) shouldBe false
      isWizard(c) shouldBe false

      de.htwg.wizard.model.value(c) shouldBe 0
    }
  }

  "helper functions" should {
    "correctly detect card types" in {
      isNormal(NormalCard(CardColor.Red, 3)) shouldBe true
      isNormal(WizardCard(CardColor.Green)) shouldBe false
      isNormal(JokerCard(CardColor.Blue)) shouldBe false

      isWizard(WizardCard(CardColor.Yellow)) shouldBe true
      isWizard(NormalCard(CardColor.Blue, 2)) shouldBe false

      isJoker(JokerCard(CardColor.Red)) shouldBe true
      isJoker(NormalCard(CardColor.Green, 1)) shouldBe false
    }
  }

  "value()" should {
    "return the number for NormalCard" in {
      de.htwg.wizard.model.value(NormalCard(CardColor.Red, 11)) shouldBe 11
    }

    "return 0 for Joker" in {
      de.htwg.wizard.model.value(JokerCard(CardColor.Blue)) shouldBe 0
    }

    "return 0 for Wizard" in {
      de.htwg.wizard.model.value(WizardCard(CardColor.Green)) shouldBe 0
    }
  }

  "determinesColor()" should {
    "return true only for NormalCard" in {
      determinesColor(NormalCard(CardColor.Red, 4)) shouldBe true
      determinesColor(WizardCard(CardColor.Blue)) shouldBe false
      determinesColor(JokerCard(CardColor.Yellow)) shouldBe false
    }
  }

  "trumpColor()" should {
    "always return a valid CardColor" in {
      val valid = CardColor.values.toSet
      for _ <- 1 to 1000 do
        valid should contain (trumpColor())
    }

    "produce different values statistically" in {
      val s = (1 to 1000).map(_ => trumpColor()).toSet
      s.size should be > 1
    }
  }

  "CardFactory" should {
    "create NormalCard from (color, value)" in {
      Card(CardColor.Green, 9) shouldBe NormalCard(CardColor.Green, 9)
    }

    "create WizardCard from (color, \"wizard\")" in {
      Card(CardColor.Red, "wizard") shouldBe WizardCard(CardColor.Red)
    }

    "create JokerCard from (color, \"joker\")" in {
      Card(CardColor.Blue, "joker") shouldBe JokerCard(CardColor.Blue)
    }

    "throw for unknown type string" in {
      assertThrows[IllegalArgumentException] {
        Card(CardColor.Yellow, "xyz")
      }
    }
  }

}
