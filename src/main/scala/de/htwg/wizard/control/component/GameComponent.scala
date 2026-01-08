package de.htwg.wizard.component.game

import de.htwg.wizard.control.*
import de.htwg.wizard.control.input.*
import de.htwg.wizard.control.strategy.*
import de.htwg.wizard.control.event.GameEvent
import de.htwg.wizard.control.observer.Observer
import de.htwg.wizard.model.*

/**
 * GameComponent encapsulates the entire game logic.
 *
 * Responsibilities:
 * - Manages observers (views)
 * - Delegates game flow to GameControl
 * - Acts as event hub between Control and View
 */
class GameComponent(
                     strategy: TrickStrategy = StandardTrickStrategy()
                   ) extends GamePort {

  // =========================================================
  // Observer registry (Component responsibility)
  // =========================================================

  private var observers: List[Observer] = Nil

  private def notifyObservers(event: GameEvent): Unit =
    observers.foreach(_.update(event))

  override def registerObserver(observer: Observer): Unit =
    observers ::= observer

  // =========================================================
  // Control (internal)
  // =========================================================

  private val control =
    new GameControl(strategy, notifyObservers)

  // =========================================================
  // Lifecycle
  // =========================================================

  override def startGame(): Unit =
    val initialState = GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = Deck(),
      currentRound = 0,
      totalRounds = 0,
      currentTrick = None,
      currentTrump = None
    )
    control.start(initialState)

  // =========================================================
  // Input handling
  // =========================================================

  override def handleInput(input: GameInput): Unit =
    input match

      case PlayerAmountSelected(n) =>
        control.submitPlayerAmount(n)

      case PredictionsSubmitted(predictions) =>
        control.submitPredictions(predictions)

      case TrickMovesSubmitted(moves) =>
        control.playTrick(moves)

      case ContinueAfterRound =>
        control.prepareNextRound()

      case Undo =>
        control.undo()

      case Redo =>
        control.redo()

      case _ =>
        ()
}
