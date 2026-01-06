package de.htwg.wizard.control.observer

import de.htwg.wizard.control.event.GameEvent


trait Observable {
  private var subscribers: Vector[Observer] = Vector()

  def add(o: Observer): Unit =
    subscribers = subscribers :+ o

  def remove(o: Observer): Unit =
    subscribers = subscribers.filterNot(_ == o)

  def notifyObservers(event: GameEvent): Unit =
    subscribers.foreach(_.update(event))
}
