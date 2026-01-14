package de.htwg.wizard.control
import de.htwg.wizard.control.GameEvent

trait Observer {
  def update(event: GameEvent): Unit
}
