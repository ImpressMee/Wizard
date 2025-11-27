package de.htwg.wizard.control

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ObservableSpec extends AnyWordSpec with Matchers {

  class MockObserver extends Observer {
    var called = 0
    override def update(): Unit = called += 1
  }

  "Observable" should {

    "remove observers so they no longer receive updates" in {
      val obs = new Observable {}         // anonyme Observable-Instanz
      val a = new MockObserver
      val b = new MockObserver

      obs.add(a)
      obs.add(b)

      obs.remove(a)                       // a wird entfernt

      obs.notifyObservers()               // nur b darf update() bekommen

      a.called shouldBe 0
      b.called shouldBe 1
    }
  }
}
