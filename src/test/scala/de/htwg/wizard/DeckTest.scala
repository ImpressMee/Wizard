package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DeckTest extends AnyWordSpec with Matchers {

  "A Deck" should {

    "deal cards correctly" in {
      val deck = new Deck()
      val dealt = deck.deal(3)
      dealt.length shouldBe 3
    }

    "shuffle without throwing" in {
      val deck = new Deck()
      noException should be thrownBy deck.shuffle()
    }
  }
}
