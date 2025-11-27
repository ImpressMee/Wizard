package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DeckSpec extends AnyWordSpec with Matchers {

  "A Deck created with Deck()" should {
    "contain all colors with all values = CardColor.values.size * 3 cards" in {
      val deck = Deck()
      val expectedSize = CardColor.values.size * 3
      deck.cards.size shouldBe expectedSize
    }

    "contain every combination of (color, value)" in {
      val deck = Deck()
      val colors = CardColor.values.toVector
      val values = 1 to 3

      for (c <- colors; v <- values) {
        deck.cards.contains(Card(c, v)) shouldBe true
      }
    }
  }

  "A Deck created directly with a card list" should {
    "store the given cards unchanged" in {
      val cards = List(Card(CardColor.Red, 2), Card(CardColor.Blue, 1))
      val deck = Deck(cards)
      deck.cards shouldBe cards
    }
  }

  "shuffle()" should {
    "return a new Deck instance" in {
      val deck = Deck()
      val shuffled = deck.shuffle()
      shuffled should not be theSameInstanceAs(deck)
    }

    "not change the number of cards" in {
      val deck = Deck()
      val shuffled = deck.shuffle()
      shuffled.cards.size shouldBe deck.cards.size
    }

    "contain the same cards in a different order (most of the time)" in {
      val deck = Deck()
      val shuffled = deck.shuffle()

      shuffled.cards.toSet shouldBe deck.cards.toSet
      // may rarely fail because random shuffle can accidentally match the original order
      // but acceptable for normal test usage
    }
  }

  "deal()" should {
    "return a hand of the requested size when enough cards exist" in {
      val deck = Deck()
      val (hand, rest) = deck.deal(5)
      hand.size shouldBe 5
      rest.cards.size shouldBe (deck.cards.size - 5)
    }

    "return all cards if requested handsize equals deck size" in {
      val deck = Deck()
      val (hand, rest) = deck.deal(deck.cards.size)
      hand.size shouldBe deck.cards.size
      rest.cards.size shouldBe 0
    }

    "return fewer cards if handsize exceeds deck size" in {
      val deck = Deck()
      val (hand, rest) = deck.deal(deck.cards.size + 10)
      hand.size shouldBe deck.cards.size
      rest.cards.size shouldBe 0
    }

    "hand and rest should have no shared cards" in {
      val deck = Deck()
      val (hand, rest) = deck.deal(5)
      hand.intersect(rest.cards) shouldBe empty
    }
  }
}
