package de.htwg.wizard.view

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import de.htwg.wizard.control.event.*

class DummyGameControl extends GameControl {
  var called: List[String] = Nil

  override def runGame(obs: de.htwg.wizard.control.observer.Observer*): Unit =
    called ::= "runGame"

  override def submitPlayerAmount(n: Int): Unit =
    called ::= s"submitPlayerAmount($n)"

  override def submitPredictions(p: Map[Int, Int]): Unit =
    called ::= "submitPredictions"

  override def playTrick(t: Int, m: Map[Int, Int]): Unit =
    called ::= "playTrick"

  override def continueAfterRound(): Unit =
    called ::= "continueAfterRound"
}
