package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DeckSpec extends AnyWordSpec with Matchers {

  "Deck()" should {
    "create a full Wizard deck (13 values × 4 colors + 4 wizards + 4 jokers = 60 cards)" in {
      val deck = Deck()  // uses Companion Object

      deck.cards.size shouldBe 60

      val normalCards = deck.cards.collect { case n: NormalCard => n }
      normalCards.size shouldBe 52  // 13 * 4

      val wizards = deck.cards.collect { case w: WizardCard => w }
      wizards.size shouldBe 4

      val jokers = deck.cards.collect { case j: JokerCard => j }
      jokers.size shouldBe 4
    }
  }

  "Deck(List)" should {
    "store cards exactly as provided" in {
      val list = List(
        NormalCard(CardColor.Red, 1),
        WizardCard(CardColor.Green)
      )
      val deck = Deck(list)

      deck.cards shouldBe list
    }
  }

  "shuffle()" should {
    "return a new deck with the same cards in different order" in {
      val deck = Deck()        // 60 cards
      val shuffled = deck.shuffle()

      shuffled.cards.toSet shouldBe deck.cards.toSet       // same content
      shuffled.cards should not equal deck.cards           // usually different order
    }

    "not mutate the original deck" in {
      val deck = Deck()
      val original = deck.cards
      deck.shuffle()
      deck.cards shouldBe original
    }
  }

  "deal()" should {

    "return correct number of cards and a reduced deck" in {
      val cards = List(
        NormalCard(CardColor.Red, 1),
        NormalCard(CardColor.Blue, 2),
        NormalCard(CardColor.Green, 3)
      )
      val deck = Deck(cards)

      val (hand, rest) = deck.deal(2)

      hand shouldBe cards.take(2)
      rest.cards shouldBe cards.drop(2)
    }

    "allow dealing 0 cards" in {
      val cards = List(
        NormalCard(CardColor.Red, 1),
        NormalCard(CardColor.Green, 2)
      )
      val deck = Deck(cards)

      val (hand, rest) = deck.deal(0)

      hand shouldBe empty
      rest.cards shouldBe cards
    }

    "deal all cards successfully" in {
      val cards = List(
        WizardCard(CardColor.Blue),
        JokerCard(CardColor.Yellow)
      )
      val deck = Deck(cards)

      val (hand, rest) = deck.deal(2)

      hand shouldBe cards
      rest.cards shouldBe empty
    }

    "deal more cards than available (returns what exists)" in {
      val cards = List(
        NormalCard(CardColor.Red, 1),
        NormalCard(CardColor.Blue, 2)
      )
      val deck = Deck(cards)

      val (hand, rest) = deck.deal(10)

      hand shouldBe cards
      rest.cards shouldBe empty
    }
  }
}
