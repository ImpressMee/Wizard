package de.htwg.wizard.model

import de.htwg.wizard.model.modelComponent.{CardColor, JokerCard, NormalCard, Trick, WizardCard}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TrickSpec extends AnyWordSpec with Matchers {

  "Trick" should {

    "store the played cards correctly" in {
      val played = Map(
        0 -> NormalCard(CardColor.Red, 5),
        1 -> WizardCard(CardColor.Blue)
      )

      val trick = Trick(played)

      trick.played shouldBe played
    }

    "support an empty trick" in {
      val trick = Trick(Map.empty)

      trick.played shouldBe empty
    }

    "support equality for identical tricks" in {
      val played = Map(
        0 -> NormalCard(CardColor.Green, 3)
      )

      val t1 = Trick(played)
      val t2 = Trick(played)

      t1 shouldBe t2
    }

    "not be equal if played cards differ" in {
      val t1 = Trick(Map(0 -> NormalCard(CardColor.Red, 1)))
      val t2 = Trick(Map(0 -> NormalCard(CardColor.Red, 2)))

      t1 should not be t2
    }

    "support copy with modified played cards" in {
      val t1 = Trick(Map(0 -> NormalCard(CardColor.Blue, 4)))

      val t2 = t1.copy(
        played = t1.played + (1 -> JokerCard(CardColor.Yellow))
      )

      t2.played.size shouldBe 2
      t2.played.contains(1) shouldBe true
    }
  }
}
