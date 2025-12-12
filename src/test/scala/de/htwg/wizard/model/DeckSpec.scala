package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DeckSpec extends AnyWordSpec with Matchers {

  "Deck case class" should {

    "be created empty when passing an empty list explicitly" in {
      val d = Deck(List())
      d.cards shouldBe empty
    }

    "be created with a given list of cards via case class apply" in {
      val cards = List(
        NormalCard(CardColor.Red, 1),
        WizardCard(CardColor.Blue)
      )
      Deck(cards).cards shouldBe cards
    }
  }

  "shuffle" should {

    "return a new Deck with the same cards" in {
      val cards = List(
        NormalCard(CardColor.Red, 1),
        NormalCard(CardColor.Green, 2),
        WizardCard(CardColor.Blue),
        JokerCard(CardColor.Yellow)
      )

      val d = Deck(cards)
      val shuffled = d.shuffle()

      shuffled.cards.toSet shouldBe cards.toSet
      shuffled.cards.size shouldBe cards.size
    }
  }

  "deal" should {

    "deal a hand and return remaining deck" in {
      val cards = List(
        NormalCard(CardColor.Red, 1),
        NormalCard(CardColor.Green, 2),
        NormalCard(CardColor.Blue, 3)
      )

      val (hand, rest) = Deck(cards).deal(2)

      hand shouldBe cards.take(2)
      rest.cards shouldBe cards.drop(2)
    }
  }

  "Deck companion apply()" should {

    "create a full wizard deck" in {
      Deck().cards.size shouldBe 60
    }
  }
}
