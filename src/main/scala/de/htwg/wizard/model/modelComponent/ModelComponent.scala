
package de.htwg.wizard.model.modelComponent

import de.htwg.wizard.model.*
import de.htwg.wizard.model.ModelInterface
import de.htwg.wizard.model.modelComponent.{Deck, GameState, GameStateMemento}

/**
 * Concrete implementation of the Model component.
 *
 * Responsibilities:
 * - Hold the current GameState
 * - Manage undo history using Memento pattern
 * - Expose state only through ModelInterface
 *
 * Internal details (history, mementos) are hidden from clients.
 */
class ModelComponent extends ModelInterface {

  // =========================================================
  // Internal state
  // =========================================================

  private var currentState: GameState =
    GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = Deck(),
      currentRound = 0,
      totalRounds = 0
    )

  private var history: List[GameStateMemento] = Nil

  // =========================================================
  // Interface implementation
  // =========================================================

  override def state: GameState =
    currentState

  override def updateState(newState: GameState): Unit =
    currentState = newState

  override def save(): Unit =
    history ::= currentState.createMemento()

  override def undo(): Unit =
    history match
      case m :: rest =>
        currentState = currentState.restore(m)
        history = rest
      case Nil =>
        () // nothing to undo
}
