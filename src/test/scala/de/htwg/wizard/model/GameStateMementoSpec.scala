package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStateMementoSpec extends AnyWordSpec with Matchers {

  val p0 = Player(0)
  val p1 = Player(1)

  val deck = Deck(List(
    NormalCard(CardColor.Red, 3),
    NormalCard(CardColor.Blue, 5)
  ))

  val trick = Trick(Map(0 -> NormalCard(CardColor.Green, 7)))

  "GameStateMemento" should {

    "store all fields correctly" in {
      val m = GameStateMemento(
        amountOfPlayers = 2,
        players = List(p0, p1),
        deck = deck,
        currentRound = 3,
        totalRounds = 7,
        currentTrump = Some(CardColor.Yellow),
        currentTrick = Some(trick)
      )

      m.amountOfPlayers shouldBe 2
      m.players shouldBe List(p0, p1)
      m.deck shouldBe deck
      m.currentRound shouldBe 3
      m.totalRounds shouldBe 7
      m.currentTrump shouldBe Some(CardColor.Yellow)
      m.currentTrick shouldBe Some(trick)
    }

    "be immutable (copy produces a new instance)" in {
      val m1 = GameStateMemento(2, List(p0), deck, 1, 5, None, None)
      val m2 = m1.copy(currentRound = 99)

      m1.currentRound shouldBe 1
      m2.currentRound shouldBe 99
    }

    "allow replacing all fields using copy" in {
      val original = GameStateMemento(2, List(p0), deck, 1, 5, None, None)

      val m = original.copy(
        amountOfPlayers = 4,
        players = List(p1),
        deck = Deck(Nil),         // NOTE: Deck() != empty deck
        currentRound = 10,
        totalRounds = 3,
        currentTrump = Some(CardColor.Blue),
        currentTrick = Some(trick)
      )

      m.amountOfPlayers shouldBe 4
      m.players shouldBe List(p1)
      m.deck.cards shouldBe empty
      m.currentRound shouldBe 10
      m.totalRounds shouldBe 3
      m.currentTrump shouldBe Some(CardColor.Blue)
      m.currentTrick shouldBe Some(trick)
    }

    "not modify original memento when using copy" in {
      val m1 = GameStateMemento(2, List(p0), deck, 1, 5, None, None)
      val m2 = m1.copy(players = List(p1))

      m1.players shouldBe List(p0)
      m2.players shouldBe List(p1)
    }

    "support equality by value" in {
      val m1 = GameStateMemento(2, List(p0), deck, 1, 5, None, None)
      val m2 = GameStateMemento(2, List(p0), deck, 1, 5, None, None)

      m1 shouldBe m2
    }

    "produce readable toString output" in {
      val m = GameStateMemento(2, List(p0), deck, 1, 5, None, None)
      val s = m.toString

      s should include ("GameStateMemento")
      s.length should be > 20     
    }

    "handle empty player lists and empty decks" in {
      val m = GameStateMemento(
        0,
        Nil,
        Deck(Nil),    
        currentRound = 0,
        totalRounds = 0,
        currentTrump = None,
        currentTrick = None
      )

      m.players shouldBe Nil
      m.deck.cards shouldBe empty
      m.currentTrump shouldBe None
      m.currentTrick shouldBe None
    }
  }
}
