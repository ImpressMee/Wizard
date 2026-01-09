package de.htwg.wizard.control.observer

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.event.PlayerAmountRequested
import de.htwg.wizard.model.*

class ObservableSpec extends AnyWordSpec with Matchers {

  class TestObserver extends Observer {
    var received: List[Any] = Nil
    override def update(event: de.htwg.wizard.control.event.GameEvent): Unit =
      received = event :: received
  }

  class TestObservable extends Observable

  "Observable" should {

    "notify a registered observer" in {
      val observable = new TestObservable
      val observer = new TestObserver
      val state = GameState(0, Nil, Deck(), 0, 0)
      val event = PlayerAmountRequested(state)

      observable.add(observer)
      observable.notifyObservers(event)

      observer.received should contain (event)
    }

    "notify multiple observers" in {
      val observable = new TestObservable
      val o1 = new TestObserver
      val o2 = new TestObserver
      val state = GameState(0, Nil, Deck(), 0, 0)
      val event = PlayerAmountRequested(state)

      observable.add(o1)
      observable.add(o2)
      observable.notifyObservers(event)

      o1.received should contain (event)
      o2.received should contain (event)
    }

    "not notify removed observers" in {
      val observable = new TestObservable
      val observer = new TestObserver
      val state = GameState(0, Nil, Deck(), 0, 0)
      val event = PlayerAmountRequested(state)

      observable.add(observer)
      observable.remove(observer)
      observable.notifyObservers(event)

      observer.received shouldBe empty
    }
  }
}
