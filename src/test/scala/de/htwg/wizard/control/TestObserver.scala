package de.htwg.wizard.control

import de.htwg.wizard.control.observer.Observer
import de.htwg.wizard.control.event.GameEvent

class RecordingObserver extends Observer {
  var events: List[GameEvent] = Nil
  override def update(event: GameEvent): Unit =
    events = event :: events
}
