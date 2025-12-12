package de.htwg.wizard.control.observer

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ObservableSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // TestObserver
  // ------------------------------------------------------------
  class TestObserver extends Observer {
    var updates = 0
    override def update(): Unit = updates += 1
  }

  "Observable" should {

    "notify a single observer" in {
      val obs = new Observable {}
      val o = new TestObserver

      obs.add(o)
      obs.notifyObservers()

      o.updates shouldBe 1
    }

    "notify multiple observers" in {
      val obs = new Observable {}
      val o1 = new TestObserver
      val o2 = new TestObserver

      obs.add(o1)
      obs.add(o2)
      obs.notifyObservers()

      o1.updates shouldBe 1
      o2.updates shouldBe 1
    }

    "notify observers multiple times" in {
      val obs = new Observable {}
      val o = new TestObserver

      obs.add(o)
      obs.notifyObservers()
      obs.notifyObservers()
      obs.notifyObservers()

      o.updates shouldBe 3
    }

    "remove an observer correctly" in {
      val obs = new Observable {}
      val o = new TestObserver

      obs.add(o)
      obs.remove(o)
      obs.notifyObservers()

      o.updates shouldBe 0
    }

    "remove only the specified observer" in {
      val obs = new Observable {}
      val o1 = new TestObserver
      val o2 = new TestObserver

      obs.add(o1)
      obs.add(o2)
      obs.remove(o1)
      obs.notifyObservers()

      o1.updates shouldBe 0
      o2.updates shouldBe 1
    }

    "ignore removing an observer that was never added" in {
      val obs = new Observable {}
      val o1 = new TestObserver
      val o2 = new TestObserver

      obs.add(o1)
      obs.remove(o2) // o2 war nie drin
      obs.notifyObservers()

      o1.updates shouldBe 1
      o2.updates shouldBe 0
    }

    "notifyObservers does nothing if there are no observers" in {
      val obs = new Observable {}

      noException should be thrownBy obs.notifyObservers()
    }
  }
}
