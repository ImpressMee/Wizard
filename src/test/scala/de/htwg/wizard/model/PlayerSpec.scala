package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSpec extends AnyWordSpec with Matchers {

  val red5 = NormalCard(CardColor.Red, 5)
  val blue7 = NormalCard(CardColor.Blue, 7)

  "A Player" should {

    "store id and have correct default values" in {
      val p = Player(id = 3)

      p.id shouldBe 3
      p.hand shouldBe List()
      p.tricks shouldBe 0
      p.totalPoints shouldBe 0
      p.predictedTricks shouldBe 0
    }

    "store a hand when provided" in {
      val p = Player(1, hand = List(red5, blue7))

      p.hand shouldBe List(red5, blue7)
      p.hand.size shouldBe 2
    }

    "store tricks, points and predicted tricks when provided" in {
      val p = Player(1, List(), tricks = 2, totalPoints = 30, predictedTricks = 1)

      p.tricks shouldBe 2
      p.totalPoints shouldBe 30
      p.predictedTricks shouldBe 1
    }

    "be immutable when modified using copy()" in {
      val p1 = Player(1, List(red5), tricks = 1)
      val p2 = p1.copy(tricks = 2)

      p1.tricks shouldBe 1
      p2.tricks shouldBe 2

      p2.id shouldBe p1.id
      p2.hand shouldBe p1.hand
    }

    "allow changing the hand using copy()" in {
      val p1 = Player(1, List(red5))
      val p2 = p1.copy(hand = List(blue7))

      p2.hand shouldBe List(blue7)
      p1.hand shouldBe List(red5)
    }

    "allow changing totalPoints using copy()" in {
      val p1 = Player(1, totalPoints = 10)
      val p2 = p1.copy(totalPoints = 40)

      p1.totalPoints shouldBe 10
      p2.totalPoints shouldBe 40
    }

    "allow changing predictedTricks using copy()" in {
      val p1 = Player(1, predictedTricks = 2)
      val p2 = p1.copy(predictedTricks = 3)

      p1.predictedTricks shouldBe 2
      p2.predictedTricks shouldBe 3
    }

    "implement a valid equals() method" in {
      val p1 = Player(1, List(red5), tricks = 1, totalPoints = 5)
      val p2 = Player(1, List(red5), tricks = 1, totalPoints = 5)

      p1 shouldEqual p2
    }

    "produce different players when any parameter differs" in {
      val base = Player(1)

      base should not equal Player(2)
      base should not equal Player(1, List(red5))
      base should not equal Player(1, List(), tricks = 1)
      base should not equal Player(1, List(), totalPoints = 5)
      base should not equal Player(1, List(), predictedTricks = 99)
    }

    "generate a stable hashCode" in {
      val p1 = Player(1, List(red5), tricks = 1)
      val p2 = Player(1, List(red5), tricks = 1)

      p1.hashCode shouldBe p2.hashCode
    }

    "not allow unintended mutation of the hand list" in {
      val hand = List(red5, blue7)
      val p = Player(1, hand)

      // try to mutate original list (Scala lists are immutable anyway)
      val mutated = red5 :: hand

      p.hand shouldBe List(red5, blue7)
      mutated should not equal p.hand
    }

    "support replacing multiple fields using copy()" in {
      val p1 = Player(1, List(red5), 1, 10, 2)

      val p2 = p1.copy(
        hand = List(blue7),
        tricks = 0,
        totalPoints = 20,
        predictedTricks = 5
      )

      p2.hand shouldBe List(blue7)
      p2.tricks shouldBe 0
      p2.totalPoints shouldBe 20
      p2.predictedTricks shouldBe 5
    }
  }
}
