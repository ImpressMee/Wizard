package de.htwg.wizard.control

import de.htwg.wizard.model.modelComponent.GameState

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

  def init(): Unit
  /**
   * Unified entry point for all user interactions.
   * This decouples input handling from concrete UI implementations.
   */
  def handleInput(input: GameInput): Unit

  def isAllowedMove(playerId: Int,
                     cardIndex: Int,
                     state: GameState
                   ): Boolean
  def canSafelyExit: Boolean
}
