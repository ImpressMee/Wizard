package de.htwg.wizard.control

trait Observable {
  private var subscribers: Vector[Observer] = Vector()

  def add(o: Observer): Unit =
    subscribers = subscribers :+ o

  def remove(o: Observer): Unit =
    subscribers = subscribers.filterNot(_ == o)

  def notifyObservers(): Unit =
    subscribers.foreach(_.update())
}
