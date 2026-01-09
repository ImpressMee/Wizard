package de.htwg.wizard.control.observer

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.event.StateChanged
import de.htwg.wizard.model.*

class ObserverSpec extends AnyWordSpec with Matchers {

  "Observer trait" should {

    "be usable via polymorphism and receive a GameEvent" in {
      var called = false

      val observer: Observer = new Observer {
        override def update(event: de.htwg.wizard.control.event.GameEvent): Unit =
          called = true
      }

      val state = GameState(0, Nil, Deck(), 0, 0)
      val event = StateChanged(state)

      observer.update(event)

      called shouldBe true
    }
  }
}
