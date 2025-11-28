package de.htwg.wizard.control

import de.htwg.wizard.model.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class StandardTrickStrategySpec extends AnyWordSpec with Matchers {

  val strategy = StandardTrickStrategy()

  // Helpers
  def t(cards: (Int, Card)*): Trick = Trick(cards.toMap)

  val r5  = NormalCard(CardColor.Red, 5)
  val r9  = NormalCard(CardColor.Red, 9)
  val b7  = NormalCard(CardColor.Blue, 7)
  val g3  = NormalCard(CardColor.Green, 3)
  val b12 = NormalCard(CardColor.Blue, 12)

  val wR  = WizardCard(CardColor.Red)
  val wG  = WizardCard(CardColor.Green)

  val jR  = JokerCard(CardColor.Red)
  val jB  = JokerCard(CardColor.Blue)

  // -------------------------------------------------------------
  // Tests
  // -------------------------------------------------------------

  "StandardTrickStrategy" should {

    "Wizard wins instantly (first wizard in order)" in {
      val trick = t(
        0 -> r5,
        1 -> wG,   // first wizard
        2 -> wR
      )

      strategy.winner(trick, Some(CardColor.Red)) shouldBe (1 -> wG)
    }

    "if all cards are Jokers, the last joker wins" in {
      val trick = t(
        0 -> jR,
        1 -> jB
      )

      strategy.winner(trick, None) shouldBe (1 -> jB)
    }

    "if only jokers and no normal cards, last card wins" in {
      val trick = t(
        0 -> jR,
        1 -> jB,
        2 -> jR
      )

      strategy.winner(trick, None) shouldBe (2 -> jR)
    }

    "highest trump wins when at least one trump appears" in {
      val trick = t(
        0 -> r5,
        1 -> b7,
        2 -> r9     // highest trump
      )

      strategy.winner(trick, Some(CardColor.Red)) shouldBe (2 -> r9)
    }

    "lead color is determined by first normal card" in {
      val trick = t(
        0 -> jR,  // ignored for lead
        1 -> b7,  // lead = Blue
        2 -> g3
      )

      strategy.winner(trick, Some(CardColor.Yellow)) shouldBe (1 -> b7)
    }

    "highest card of the lead color wins when no trump played" in {
      val trick = t(
        0 -> b7,     // lead color Blue
        1 -> g3,
        2 -> b12     // highest Blue
      )

      strategy.winner(trick, None) shouldBe (2 -> b12)
    }

    "fallback: last joker wins if no normal cards exist (and no wizard)" in {
      val trick = t(
        0 -> jR,
        1 -> jB
      )

      strategy.winner(trick, None) shouldBe (1 -> jB)
    }

    "normal cards only: highest of lead color wins (no trump)" in {
      val trick = t(
        0 -> g3,  // lead = Green
        1 -> r5,
        2 -> NormalCard(CardColor.Green, 10)
      )

      strategy.winner(trick, None) shouldBe (2 -> NormalCard(CardColor.Green, 10))
    }

    "trump beats lead color always" in {
      val trick = t(
        0 -> b7,   // lead = Blue
        1 -> r5,   // trump
        2 -> b12
      )

      strategy.winner(trick, Some(CardColor.Red)) shouldBe (1 -> r5)
    }
  }
}
