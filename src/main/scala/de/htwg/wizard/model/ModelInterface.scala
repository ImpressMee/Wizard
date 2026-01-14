package de.htwg.wizard.model

import de.htwg.wizard.model.modelComponent.GameState

/**
 * Public interface of the Model component.
 *
 * Provides read-only access to the game state and
 * controlled state updates for the controller.
 *
 * Stable outside, flexible inside.
 */
trait ModelInterface {

  // =========================================================
  // State access
  // =========================================================

  /** Returns the current game state. */
  def state: GameState

  // =========================================================
  // State lifecycle
  // =========================================================

  /** Replaces the current game state. */
  def updateState(newState: GameState): Unit

  // =========================================================
  // Undo / Redo (optional, but component-clean)
  // =========================================================

  /** Saves the current state to the history. */
  def save(): Unit

  /** Restores the previous state if available. */
  def undo(): Unit
}
