package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStateSpec extends AnyWordSpec with Matchers {

  "A GameState" should {

    "store all constructor parameters correctly" in {
      val players = List(Player(1), Player(2))
      val deck = Deck()
      val trick = Trick(Map(1 -> Card(CardColor.Red, 2)))

      val gs = GameState(
        amountOfPlayers = 2,
        players = players,
        deck = deck,
        currentRound = 1,
        totalRounds = 20,
        currentTrump = CardColor.Blue,
        currentTrick = Some(trick)
      )

      gs.amountOfPlayers shouldBe 2
      gs.players shouldBe players
      gs.deck shouldBe deck
      gs.currentRound shouldBe 1
      gs.totalRounds shouldBe 20
      gs.currentTrump shouldBe CardColor.Blue
      gs.currentTrick shouldBe Some(trick)
    }

    "allow currentTrick to be None" in {
      val gs = GameState(
        amountOfPlayers = 3,
        players = List(Player(1), Player(2), Player(3)),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 20,
        currentTrump = CardColor.Red,
        currentTrick = None
      )

      gs.currentTrick shouldBe None
    }

    "support immutability using copy (change round)" in {
      val gs = GameState(3, Nil, Deck(), 1, 20, CardColor.Green)
      val updated = gs.copy(currentRound = 5)

      updated.currentRound shouldBe 5
      gs.currentRound shouldBe 1
    }

    "support immutability using copy (change trump)" in {
      val gs = GameState(3, Nil, Deck(), 1, 20, CardColor.Green)
      val updated = gs.copy(currentTrump = CardColor.Yellow)

      updated.currentTrump shouldBe CardColor.Yellow
      gs.currentTrump shouldBe CardColor.Green
    }

    "support immutability using copy (change trick)" in {
      val gs = GameState(3, Nil, Deck(), 1, 20, CardColor.Red)
      val trick = Trick(Map(1 -> Card(CardColor.Blue, 3)))
      val updated = gs.copy(currentTrick = Some(trick))

      updated.currentTrick shouldBe Some(trick)
      gs.currentTrick shouldBe None
    }

    "be equal when all fields are equal" in {
      val gs1 = GameState(3, Nil, Deck(), 1, 20, CardColor.Red)
      val gs2 = GameState(3, Nil, Deck(), 1, 20, CardColor.Red)

      // Deck() creates a new shuffled list only once per call,
      // so we must compare decks also directly
      gs1.copy(deck = gs2.deck) shouldBe gs2
    }

    "not be equal when any field differs" in {
      val gs1 = GameState(3, Nil, Deck(), 1, 20, CardColor.Red)
      val gs2 = gs1.copy(currentRound = 2)

      gs1 should not be gs2
    }
  }
}
