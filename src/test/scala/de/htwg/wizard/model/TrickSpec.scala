package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TrickSpec extends AnyWordSpec with Matchers {

  val red5   = NormalCard(CardColor.Red, 5)
  val blue7  = NormalCard(CardColor.Blue, 7)
  val wizG   = WizardCard(CardColor.Green)
  val jokeY  = JokerCard(CardColor.Yellow)

  "A Trick" should {

    "store played cards in a map" in {
      val t = Trick(Map(0 -> red5, 1 -> blue7))
      t.played shouldBe Map(0 -> red5, 1 -> blue7)
    }

    "be empty when created with an empty map" in {
      val empty = Trick(Map())

      empty.played shouldBe Map()
      empty.played.isEmpty shouldBe true
    }

    "allow a single entry" in {
      val t = Trick(Map(2 -> wizG))

      t.played.size shouldBe 1
      t.played(2) shouldBe wizG
    }

    "store any Card type: Normal, Wizard, Joker" in {
      val t = Trick(Map(
        0 -> red5,
        1 -> wizG,
        2 -> jokeY
      ))

      t.played(0) shouldBe red5
      t.played(1) shouldBe wizG
      t.played(2) shouldBe jokeY
    }

    "use PlayerID as keys" in {
      val t = Trick(Map(0 -> red5, 5 -> blue7))

      t.played.keys should contain allOf (0, 5)
    }

    "be immutable when modified using copy()" in {
      val t1 = Trick(Map(0 -> red5))
      val t2 = t1.copy(played = t1.played + (1 -> blue7))

      t1.played shouldBe Map(0 -> red5)
      t2.played shouldBe Map(0 -> red5, 1 -> blue7)
    }

    "not allow duplicate player IDs in a Map (last wins – Map behavior)" in {
      val t = Trick(Map(
        0 -> red5,
        0 -> blue7 // overwrites red5
      ))

      t.played.size shouldBe 1
      t.played(0) shouldBe blue7
    }

    "support adding cards by creating a new Trick" in {
      val t1 = Trick(Map(0 -> red5))
      val t2 = Trick(t1.played + (1 -> blue7))

      t1.played shouldBe Map(0 -> red5)
      t2.played shouldBe Map(0 -> red5, 1 -> blue7)
    }

    "compare equal if maps are equal" in {
      val t1 = Trick(Map(0 -> red5, 1 -> blue7))
      val t2 = Trick(Map(0 -> red5, 1 -> blue7))

      t1 shouldBe t2
      t1.hashCode shouldBe t2.hashCode
    }

    "not compare equal if maps differ" in {
      val t1 = Trick(Map(0 -> red5))
      val t2 = Trick(Map(0 -> blue7))

      t1 should not equal t2
    }

    "ignore order in the internal map (because Map is unordered)" in {
      val t1 = Trick(Map(0 -> red5, 1 -> blue7))
      val t2 = Trick(Map(1 -> blue7, 0 -> red5))

      t1 shouldBe t2
    }

    "handle large number of players" in {
      val bigMap = (0 to 20).map(i => i -> NormalCard(CardColor.Red, i+1)).toMap
      val t = Trick(bigMap)

      t.played.size shouldBe 21
    }

  }
}
