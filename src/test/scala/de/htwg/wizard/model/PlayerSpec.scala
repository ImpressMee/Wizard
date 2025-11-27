package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSpec extends AnyWordSpec with Matchers {

  "A Player" should {

    "store all constructor parameters correctly" in {
      val hand = List(Card(CardColor.Red, 2), Card(CardColor.Green, 1))
      val p = Player(
        id = 1,
        hand = hand,
        tricks = 3,
        totalPoints = 15,
        predictedTricks = 2
      )

      p.id shouldBe 1
      p.hand shouldBe hand
      p.tricks shouldBe 3
      p.totalPoints shouldBe 15
      p.predictedTricks shouldBe 2
    }

    "use default values when only id is given" in {
      val p = Player(5)

      p.id shouldBe 5
      p.hand shouldBe empty
      p.tricks shouldBe 0
      p.totalPoints shouldBe 0
      p.predictedTricks shouldBe 0
    }

    "copy changes (changing hand)" in {
      val p = Player(1)
      val newHand = List(Card(CardColor.Blue, 3))

      val updated = p.copy(hand = newHand)

      updated.hand shouldBe newHand
      updated.id shouldBe p.id
      updated.tricks shouldBe p.tricks
      updated.totalPoints shouldBe p.totalPoints
      updated.predictedTricks shouldBe p.predictedTricks
    }

    "support immutability and copy changes (changing tricks)" in {
      val p = Player(1, tricks = 1)
      val updated = p.copy(tricks = 4)

      updated.tricks shouldBe 4
      updated.hand shouldBe p.hand
      updated.totalPoints shouldBe p.totalPoints
      updated.predictedTricks shouldBe p.predictedTricks
    }

    "support immutability and copy changes (changing totalPoints)" in {
      val p = Player(1, totalPoints = 10)
      val updated = p.copy(totalPoints = 30)

      updated.totalPoints shouldBe 30
      updated.id shouldBe p.id
      updated.hand shouldBe p.hand
      updated.tricks shouldBe p.tricks
      updated.predictedTricks shouldBe p.predictedTricks
    }

    "support immutability and copy changes (changing predictedTricks)" in {
      val p = Player(1, predictedTricks = 2)
      val updated = p.copy(predictedTricks = 5)

      updated.predictedTricks shouldBe 5
      updated.id shouldBe p.id
      updated.hand shouldBe p.hand
      updated.tricks shouldBe p.tricks
      updated.totalPoints shouldBe p.totalPoints
    }

    "be equal when all fields are equal" in {
      val p1 = Player(1, List(Card(CardColor.Red, 1)), 2, 10, 1)
      val p2 = Player(1, List(Card(CardColor.Red, 1)), 2, 10, 1)

      p1 shouldBe p2
    }

    "not be equal when any field differs" in {
      val p1 = Player(1)
      val p2 = Player(2)

      p1 should not be p2
    }
  }
}
