package de.htwg.wizard.component.game

import de.htwg.wizard.control.input.GameInput
import de.htwg.wizard.control.observer.Observer

/**
 * GamePort is the stable interface (port) of the GameComponent.
 * Views interact exclusively through this interface.
 *
 * The implementation behind this interface may change
 * without affecting clients.
 */
trait GamePort {

  /** Registers an observer (GUI, TUI, tests, etc.). */
  def registerObserver(observer: Observer): Unit

  /** Starts the game with an initial state. */
  def startGame(): Unit

  /**
   * Unified entry point for all user interactions.
   * This decouples input handling from concrete UI implementations.
   */
  def handleInput(input: GameInput): Unit
}
