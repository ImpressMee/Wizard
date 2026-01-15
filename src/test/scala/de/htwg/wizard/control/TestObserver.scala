package de.htwg.wizard.control

class TestObserver extends Observer {
  var events: List[GameEvent] = Nil
  override def update(event: GameEvent): Unit =
    events = event :: events
}
