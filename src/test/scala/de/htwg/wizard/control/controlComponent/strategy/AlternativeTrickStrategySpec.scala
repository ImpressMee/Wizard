package de.htwg.wizard.control.controlComponent.strategy

import de.htwg.wizard.control.controlComponent.strategy.AlternativeTrickStrategy
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Player, Trick}

class AlternativeTrickStrategySpec extends AnyWordSpec with Matchers {

  val strategy = new AlternativeTrickStrategy

  // ---------------------------------------------------------
  // Helper cards
  // ---------------------------------------------------------
  val red5    = Card(CardColor.Red, 5)
  val red10   = Card(CardColor.Red, 10)
  val blue7  = Card(CardColor.Blue, 7)
  val green9 = Card(CardColor.Green, 9)

  val joker = Card(CardColor.Blue, "Joker")
  val wizard = Card(CardColor.Red, "Wizard")


  // ---------------------------------------------------------
  // winner(...)
  // ---------------------------------------------------------
  "AlternativeTrickStrategy.winner" should {

    "return the first wizard if any wizard is played" in {
      val trick = Trick(
        Map(
          0 -> red10,
          1 -> wizard,
          2 -> joker
        )
      )

      strategy.winner(trick, None)._1 shouldBe 1
    }

    "return the last joker if no wizard is present" in {
      val trick = Trick(
        Map(
          0 -> joker,
          1 -> red5,
          2 -> joker
        )
      )

      strategy.winner(trick, None)._1 shouldBe 2
    }

    "return highest trump card if no wizard or joker exists" in {
      val trick = Trick(
        Map(
          0 -> red5,
          1 -> blue7,
          2 -> red10
        )
      )

      strategy.winner(trick, Some(CardColor.Red))._1 shouldBe 2
    }

    "return highest normal card if no trump is present" in {
      val trick = Trick(
        Map(
          0 -> red5,
          1 -> blue7,
          2 -> green9
        )
      )

      strategy.winner(trick, None)._1 shouldBe 2
    }
  }

  // ---------------------------------------------------------
  // isAllowedMove(...)
  // ---------------------------------------------------------
  "AlternativeTrickStrategy.isAllowedMove" should {

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
        Player(0, hand = List(blue7, green9))

      val trick =
        Trick(Map(1 -> red10))

      strategy.isAllowedMove(blue7, player, trick) shouldBe true
    }
  }
}
