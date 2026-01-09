package de.htwg.wizard.control.strategy

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*

class StandardTrickStrategySpec extends AnyWordSpec with Matchers {

  val strategy = new StandardTrickStrategy

  // ---------------------------------------------------------
  // Helper cards (API-konform!)
  // ---------------------------------------------------------
  val red5    = Card(CardColor.Red, 5)
  val red10   = Card(CardColor.Red, 10)
  val blue7  = Card(CardColor.Blue, 7)
  val blue9  = Card(CardColor.Blue, 9)
  val green8 = Card(CardColor.Green, 8)

  val wizard = Card(CardColor.Red, "Wizard")
  val joker  = Card(CardColor.Blue, "Joker")

  // ---------------------------------------------------------
  // winner(...)
  // ---------------------------------------------------------
  "StandardTrickStrategy.winner" should {

    "return the first wizard if a wizard is played" in {
      val trick = Trick(
        Map(
          0 -> red10,
          1 -> wizard,
          2 -> joker
        )
      )

      strategy.winner(trick, None)._1 shouldBe 1
    }

    "return the last joker if all cards are jokers" in {
      val trick = Trick(
        Map(
          0 -> joker,
          1 -> joker,
          2 -> joker
        )
      )

      strategy.winner(trick, None)._1 shouldBe 2
    }

    "use lead color if no trump is present" in {
      val trick = Trick(
        Map(
          0 -> red5,
          1 -> blue9,
          2 -> red10
        )
      )

      strategy.winner(trick, None)._1 shouldBe 2
    }

    "prefer trump cards over lead color" in {
      val trick = Trick(
        Map(
          0 -> red10,
          1 -> blue7,
          2 -> blue9
        )
      )

      strategy.winner(trick, Some(CardColor.Blue))._1 shouldBe 2
    }

    "return last card if no normal card exists" in {
      val trick = Trick(
        Map(
          0 -> joker,
          1 -> wizard
        )
      )

      strategy.winner(trick, None)._1 shouldBe 1
    }
  }

  // ---------------------------------------------------------
  // isAllowedMove(...)
  // ---------------------------------------------------------
  "StandardTrickStrategy.isAllowedMove" should {

    "allow any card if trick is empty" in {
      val player = Player(0, hand = List(red5, blue7))
      val trick  = Trick(Map.empty)

      strategy.isAllowedMove(red5, player, trick) shouldBe true
    }

    "allow any card if lead card is wizard or joker" in {
      val player = Player(0, hand = List(red5))
      val trick  = Trick(Map(1 -> joker))

      strategy.isAllowedMove(red5, player, trick) shouldBe true
    }

    "force following suit if player has lead color" in {
      val player =
        Player(0, hand = List(red5, blue7))

      val trick =
        Trick(Map(1 -> red10))

      strategy.isAllowedMove(blue7, player, trick) shouldBe false
      strategy.isAllowedMove(red5, player, trick) shouldBe true
    }

    "allow any card if player does not have lead color" in {
      val player =
        Player(0, hand = List(blue7, green8))

      val trick =
        Trick(Map(1 -> red10))

      strategy.isAllowedMove(blue7, player, trick) shouldBe true
    }
  }
}
