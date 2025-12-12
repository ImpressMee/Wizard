package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSpec extends AnyWordSpec with Matchers {

  "Player" should {

    "store all constructor parameters correctly" in {
      val cards = List(
        NormalCard(CardColor.Red, 3),
        WizardCard(CardColor.Blue)
      )

      val p = Player(
        id = 1,
        hand = cards,
        tricks = 2,
        totalPoints = 40,
        predictedTricks = 3
      )

      p.id shouldBe 1
      p.hand shouldBe cards
      p.tricks shouldBe 2
      p.totalPoints shouldBe 40
      p.predictedTricks shouldBe 3
    }

    "use default values when only id is provided" in {
      val p = Player(0)

      p.id shouldBe 0
      p.hand shouldBe empty
      p.tricks shouldBe 0
      p.totalPoints shouldBe 0
      p.predictedTricks shouldBe 0
    }

    "support equality for identical players" in {
      val p1 = Player(1)
      val p2 = Player(1)

      p1 shouldBe p2
    }

    "not be equal if any field differs" in {
      val p1 = Player(1)
      val p2 = Player(1, tricks = 1)

      p1 should not be p2
    }

    "support copy with modified fields" in {
      val p = Player(2)

      val updated = p.copy(
        tricks = 1,
        totalPoints = 10,
        predictedTricks = 2
      )

      updated.id shouldBe 2
      updated.tricks shouldBe 1
      updated.totalPoints shouldBe 10
      updated.predictedTricks shouldBe 2
    }
  }
}
