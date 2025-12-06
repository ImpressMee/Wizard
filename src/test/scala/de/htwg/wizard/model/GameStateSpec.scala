package de.htwg.wizard.model

import de.htwg.wizard.control.observer.Observer
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStateSpec extends AnyWordSpec with Matchers {

  // --------------------------------------------------------
  // Helper: MockObserver to test Observable behavior
  // --------------------------------------------------------

  class MockObserver extends Observer {
    var updates = 0
    override def update(): Unit = updates += 1
  }

  // Sample data
  val p0 = Player(0)
  val p1 = Player(1)

  val deck = Deck(List(
    NormalCard(CardColor.Red, 1),
    NormalCard(CardColor.Blue, 2)
  ))

  val trick = Trick(Map(
    0 -> NormalCard(CardColor.Red, 1)
  ))

  // --------------------------------------------------------
  // TESTS
  // --------------------------------------------------------

  "GameState" should {

    "store all fields correctly" in {
      val gs = GameState(
        amountOfPlayers = 2,
        players = List(p0, p1),
        deck = deck,
        currentRound = 1,
        totalRounds = 10,
        currentTrump = Some(CardColor.Green),
        currentTrick = Some(trick)
      )

      gs.amountOfPlayers shouldBe 2
      gs.players shouldBe List(p0, p1)
      gs.deck shouldBe deck
      gs.currentRound shouldBe 1
      gs.totalRounds shouldBe 10
      gs.currentTrump shouldBe Some(CardColor.Green)
      gs.currentTrick shouldBe Some(trick)
    }

    "default currentTrick to None when not specified" in {
      val gs = GameState(2, List(p0, p1), deck, 1, 10, None)
      gs.currentTrick shouldBe None
    }

    "copy itself with modified fields (immutability test)" in {
      val gs1 = GameState(2, List(p0, p1), deck, 1, 10, None)
      val gs2 = gs1.copy(currentRound = 2)

      gs1.currentRound shouldBe 1
      gs2.currentRound shouldBe 2
    }

    "copy the players list immutably" in {
      val gs1 = GameState(2, List(p0), deck, 1, 10, None)
      val gs2 = gs1.copy(players = List(p1))

      gs1.players shouldBe List(p0)
      gs2.players shouldBe List(p1)
    }

    "copy the deck immutably" in {
      val gs1 = GameState(2, List(p0), deck, 1, 10, None)
      val newDeck = Deck(List(NormalCard(CardColor.Green, 7)))
      val gs2 = gs1.copy(deck = newDeck)

      gs1.deck shouldBe deck
      gs2.deck shouldBe newDeck
    }

    "copy the trump immutably" in {
      val gs1 = GameState(2, List(p0), deck, 1, 10, None)
      val gs2 = gs1.copy(currentTrump = Some(CardColor.Yellow))

      gs1.currentTrump shouldBe None
      gs2.currentTrump shouldBe Some(CardColor.Yellow)
    }

    "copy the trick immutably" in {
      val gs1 = GameState(2, List(p0), deck, 1, 10, None)
      val gs2 = gs1.copy(currentTrick = Some(trick))

      gs1.currentTrick shouldBe None
      gs2.currentTrick shouldBe Some(trick)
    }

    "not modify the original state when using copy (deep immutability test)" in {
      val original = GameState(2, List(p0), deck, 1, 10, None)

      val changed = original.copy(
        amountOfPlayers = 3,
        players = List(p1),
        deck = Deck(),
        currentRound = 9,
        totalRounds = 1,
        currentTrump = Some(CardColor.Red),
        currentTrick = Some(trick)
      )

      original.amountOfPlayers shouldBe 2
      original.players shouldBe List(p0)
      original.deck shouldBe deck
      original.currentRound shouldBe 1
      original.totalRounds shouldBe 10
      original.currentTrump shouldBe None
      original.currentTrick shouldBe None

      changed.amountOfPlayers shouldBe 3
      changed.players shouldBe List(p1)
    }

    // --------------------------------------------------------
    // MEMENTO TESTS
    // --------------------------------------------------------

    "create a correct memento" in {
      val gs = GameState(
        2, List(p0, p1), deck, 1, 10, Some(CardColor.Blue), Some(trick)
      )

      val m = gs.createMemento()

      m.amountOfPlayers shouldBe 2
      m.players shouldBe List(p0, p1)
      m.deck shouldBe deck
      m.currentRound shouldBe 1
      m.totalRounds shouldBe 10
      m.currentTrump shouldBe Some(CardColor.Blue)
      m.currentTrick shouldBe Some(trick)
    }

    "restore correctly from a memento" in {
      val original = GameState(
        2, List(p0), deck, 1, 10, None, None
      )
      val m = GameStateMemento(
        3, List(p1), Deck(), 4, 2, Some(CardColor.Green), Some(trick)
      )

      val restored = original.restore(m)

      restored.amountOfPlayers shouldBe 3
      restored.players shouldBe List(p1)
      restored.deck shouldBe Deck()
      restored.currentRound shouldBe 4
      restored.totalRounds shouldBe 2
      restored.currentTrump shouldBe Some(CardColor.Green)
      restored.currentTrick shouldBe Some(trick)
    }

    // --------------------------------------------------------
    // OBSERVABLE TESTS
    // --------------------------------------------------------

    "notify observers" in {
      val gs = GameState(2, List(p0), deck, 1, 10, None)

      val obs1 = new MockObserver
      val obs2 = new MockObserver

      gs.add(obs1)
      gs.add(obs2)

      gs.notifyObservers()

      obs1.updates shouldBe 1
      obs2.updates shouldBe 1
    }

    "not notify removed observers" in {
      val gs = GameState(2, List(p0), deck, 1, 10, None)

      val obs1 = new MockObserver
      val obs2 = new MockObserver

      gs.add(obs1)
      gs.add(obs2)
      gs.remove(obs1)

      gs.notifyObservers()

      obs1.updates shouldBe 0
      obs2.updates shouldBe 1
    }

    "support multiple notifications" in {
      val gs = GameState(2, List(p0), deck, 1, 10, None)

      val obs = new MockObserver

      gs.add(obs)

      gs.notifyObservers()
      gs.notifyObservers()
      gs.notifyObservers()

      obs.updates shouldBe 3
    }

    "ignore removing observers not in the list" in {
      val gs = GameState(2, List(p0), deck, 1, 10, None)

      val obs1 = new MockObserver
      val obs2 = new MockObserver

      gs.remove(obs1) // should not crash
      gs.add(obs2)
      gs.remove(obs1) // again not in list

      gs.notifyObservers()

      obs1.updates shouldBe 0
      obs2.updates shouldBe 1
    }
  }
}
