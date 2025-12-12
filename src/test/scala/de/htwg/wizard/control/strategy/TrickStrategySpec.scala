package de.htwg.wizard.control.strategy

import de.htwg.wizard.control.strategy.{AlternativeTrickStrategy, StandardTrickStrategy}
import de.htwg.wizard.model.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TrickStrategySpec extends AnyWordSpec with Matchers {

  val std = StandardTrickStrategy()
  val alt = AlternativeTrickStrategy()

  // Helper
  def trick(cards: (Int, Card)*): Trick =
    Trick(cards.toMap)

  val r5   = NormalCard(CardColor.Red, 5)
  val r10  = NormalCard(CardColor.Red, 10)
  val b7   = NormalCard(CardColor.Blue, 7)
  val g3   = NormalCard(CardColor.Green, 3)
  val wR   = WizardCard(CardColor.Red)
  val wG   = WizardCard(CardColor.Green)
  val jR   = JokerCard(CardColor.Red)
  val jB   = JokerCard(CardColor.Blue)

  // ------------------------------------------------------------------
  // StandardTrickStrategy
  // ------------------------------------------------------------------
  "StandardTrickStrategy" should {

    "Wizard wins immediately" in {
      val t = trick(
        0 -> r5,
        1 -> wG,
        2 -> b7
      )

      std.winner(t, Some(CardColor.Red)) shouldBe (1 -> wG)
    }

    "if multiple Wizards exist, first wins" in {
      val t = trick(
        0 -> wR,
        1 -> wG
      )

      std.winner(t, None) shouldBe (0 -> wR)
    }

    "if all cards are Jokers, last Joker wins" in {
      val t = trick(
        0 -> jR,
        1 -> jB
      )

      std.winner(t, None) shouldBe (1 -> jB)
    }

    "highest trump wins when trump exists" in {
      val t = trick(
        0 -> r5,    // trump
        1 -> r10,   // higher trump
        2 -> b7
      )

      std.winner(t, Some(CardColor.Red)) shouldBe (1 -> r10)
    }

    "normal cards follow lead color when no trump appears" in {
      val t = trick(
        0 -> b7,    // lead color Blue
        1 -> g3,
        2 -> NormalCard(CardColor.Blue, 12)
      )

      std.winner(t, Some(CardColor.Red)) shouldBe (2 -> NormalCard(CardColor.Blue, 12))
    }

    "lead color ignored if no normal cards exist" in {
      val t = trick(
        0 -> jR,
        1 -> jB
      )

      std.winner(t, None) shouldBe (1 -> jB)
    }

    "fallback: last card wins if only jokers and no wizard" in {
      val t = trick(
        0 -> jR,
        1 -> jB
      )

      std.winner(t, None) shouldBe (1 -> jB)
    }
  }

  // ------------------------------------------------------------------
  // AlternativeTrickStrategy
  // ------------------------------------------------------------------
  "AlternativeTrickStrategy" should {

    "Wizard wins immediately" in {
      val t = trick(
        0 -> r5,
        1 -> wR,
        2 -> b7
      )

      alt.winner(t, None) shouldBe (1 -> wR)
    }

    "last Joker wins if at least one Joker is in the trick" in {
      val t = trick(
        0 -> r5,
        1 -> jR,
        2 -> jB
      )

      alt.winner(t, None) shouldBe (2 -> jB)
    }

    "highest trump wins" in {
      val t = trick(
        0 -> r5,
        1 -> r10,
        2 -> b7
      )

      alt.winner(t, Some(CardColor.Red)) shouldBe (1 -> r10)
    }

    "ignores lead color completely (normal cards compared by value only)" in {
      val t = trick(
        0 -> g3,  // lead green but irrelevant
        1 -> r5,
        2 -> NormalCard(CardColor.Blue, 12)
      )

      alt.winner(t, None) shouldBe (2 -> NormalCard(CardColor.Blue, 12))
    }

    "fallback: highest normal card wins when no trump" in {
      val t = trick(
        0 -> b7,
        1 -> g3,
        2 -> r10
      )

      alt.winner(t, None) shouldBe (2 -> r10)
    }
  }
}
