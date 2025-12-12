package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.observer.Observer

class GameStateSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // TestObserver für Observable-Verhalten
  // ------------------------------------------------------------
  class TestObserver extends Observer {
    var updated = false
    override def update(): Unit = updated = true
  }

  "GameState" should {

    "store all constructor values correctly" in {
      val players = List(Player(0), Player(1))
      val deck = Deck()
      val trick = Trick(Map(0 -> NormalCard(CardColor.Red, 5)))

      val gs = GameState(
        amountOfPlayers = 2,
        players = players,
        deck = deck,
        currentRound = 3,
        totalRounds = 10,
        currentTrump = Some(CardColor.Blue),
        currentTrick = Some(trick)
      )

      gs.amountOfPlayers shouldBe 2
      gs.players shouldBe players
      gs.deck shouldBe deck
      gs.currentRound shouldBe 3
      gs.totalRounds shouldBe 10
      gs.currentTrump shouldBe Some(CardColor.Blue)
      gs.currentTrick shouldBe Some(trick)
    }

    "create a correct GameStateMemento snapshot" in {
      val players = List(Player(0))
      val deck = Deck()
      val trick = Trick(Map())

      val gs = GameState(
        amountOfPlayers = 1,
        players = players,
        deck = deck,
        currentRound = 1,
        totalRounds = 5,
        currentTrump = None,
        currentTrick = Some(trick)
      )

      val m = gs.createMemento()

      m.amountOfPlayers shouldBe 1
      m.players shouldBe players
      m.deck shouldBe deck
      m.currentRound shouldBe 1
      m.totalRounds shouldBe 5
      m.currentTrump shouldBe None
      m.currentTrick shouldBe Some(trick)
    }

    "restore a previous state from a memento" in {
      val original = GameState(
        amountOfPlayers = 2,
        players = List(Player(0), Player(1)),
        deck = Deck(),
        currentRound = 2,
        totalRounds = 10,
        currentTrump = Some(CardColor.Red),
        currentTrick = None
      )

      val modified = GameState(
        amountOfPlayers = 1,
        players = List(Player(0)),
        deck = Deck(),
        currentRound = 5,
        totalRounds = 5,
        currentTrump = None,
        currentTrick = Some(Trick(Map()))
      )

      val restored = modified.restore(original.createMemento())

      restored shouldBe original
    }

    "notify observers when inherited Observable behavior is used" in {
      val observer = new TestObserver

      val gs = GameState(
        amountOfPlayers = 1,
        players = List(Player(0)),
        deck = Deck(),
        currentRound = 0,
        totalRounds = 0,
        currentTrump = None
      )

      gs.add(observer)
      gs.notifyObservers()

      observer.updated shouldBe true
    }
  }
}
