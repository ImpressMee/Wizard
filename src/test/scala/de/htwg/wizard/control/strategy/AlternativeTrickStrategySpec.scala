package de.htwg.wizard.control.strategy

import de.htwg.wizard.control.strategy.AlternativeTrickStrategy
import de.htwg.wizard.model.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AlternativeTrickStrategySpec extends AnyWordSpec with Matchers {

  val strat = new AlternativeTrickStrategy

  def N(c: CardColor, v: Int) = NormalCard(c, v)
  def W(c: CardColor) = WizardCard(c)
  def J(c: CardColor) = JokerCard(c)

  // -------------------------------------------------------
  // TESTS
  // -------------------------------------------------------

  "AlternativeTrickStrategy" should {

    "immediately pick the first wizard if any wizard is played" in {
      val trick = Trick(Map(
        0 -> N(CardColor.Red, 5),
        1 -> W(CardColor.Blue),
        2 -> N(CardColor.Green, 7),
        3 -> W(CardColor.Red) // ignored, first wizard wins
      ))

      val (winner, card) = strat.winner(trick, Some(CardColor.Green))

      winner shouldBe 1
      card shouldBe W(CardColor.Blue)
    }

    "pick the LAST joker if at least one joker is present and NO wizard" in {
      val trick = Trick(Map(
        0 -> J(CardColor.Blue),
        1 -> N(CardColor.Red, 6),
        2 -> J(CardColor.Green) // last joker → winner
      ))

      val (winner, card) = strat.winner(trick, Some(CardColor.Blue))

      winner shouldBe 2
      card shouldBe J(CardColor.Green)
    }

    "pick the highest trump card when no wizard/joker" in {
      val trick = Trick(Map(
        0 -> N(CardColor.Red, 3),
        1 -> N(CardColor.Green, 7),
        2 -> N(CardColor.Green, 10) // highest trump
      ))

      val (winner, card) = strat.winner(trick, Some(CardColor.Green))

      winner shouldBe 2
      card shouldBe N(CardColor.Green, 10)
    }

    "pick the highest NORMAL card when no trump exists" in {
      val trick = Trick(Map(
        0 -> N(CardColor.Red, 3),
        1 -> N(CardColor.Yellow, 10),
        2 -> N(CardColor.Green, 7)
      ))

      val (winner, card) = strat.winner(trick, None)

      winner shouldBe 1
      card shouldBe N(CardColor.Yellow, 10)
    }

    "fallback: return last card when only impossible case happens" in {
      // This case happens only if all cards are non-normal AND non-joker AND no wizard 
      val trick = Trick(Map(
        0 -> J(CardColor.Red),
        1 -> J(CardColor.Blue)
      ))

      val (winner, card) = strat.winner(trick, None)

      winner shouldBe 1
      card shouldBe J(CardColor.Blue)
    }
  }
}
