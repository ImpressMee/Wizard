package de.htwg.wizard.control
import de.htwg.wizard.control.GameInput

/**
 * All user inputs that can be sent from a view (GUI / TUI)
 * to the game component.
 *
 * This ADT represents the complete input protocol
 * of the GameComponent.
 */
sealed trait GameInput

// =========================================================
// Game setup
// =========================================================

final case class PlayerAmountSelected(amount: Int) extends GameInput

// =========================================================
// Prediction phase
// =========================================================

final case class PredictionsSubmitted(
                                       predictions: Map[Int, Int]
                                     ) extends GameInput

// =========================================================
// Trick phase
// =========================================================

final case class TrickMovesSubmitted(
                                      moves: Map[Int, Int]
                                    ) extends GameInput

// =========================================================
// Round / game flow
// =========================================================

case object ContinueAfterRound extends GameInput

// =========================================================
// Optional controls
// =========================================================

case object Undo extends GameInput
case object Redo extends GameInput
case object LoadGame extends GameInput
