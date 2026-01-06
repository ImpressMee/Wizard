package de.htwg.wizard.control.observer

import de.htwg.wizard.control.event.GameEvent

trait Observer {
  def update(event: GameEvent): Unit
}
