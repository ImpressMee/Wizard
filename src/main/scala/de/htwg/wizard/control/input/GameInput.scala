package de.htwg.wizard.control.input

/**
 * All user inputs that can be sent from a view (GUI / TUI)
 * to the game component.
 *
 * The set is intentionally minimal and matches exactly
 * the inputs handled by GameComponent.
 */
sealed trait GameInput

// =========================================================
// Game setup
// =========================================================

case class PlayerAmountSelected(amount: Int) extends GameInput

// =========================================================
// Prediction phase
// =========================================================

case class PredictionsSubmitted(predictions: Map[Int, Int]) extends GameInput

// =========================================================
// Trick phase
// =========================================================

case class TrickMovesSubmitted(moves: Map[Int, Int]) extends GameInput

// =========================================================
// Round / game flow
// =========================================================

case object ContinueAfterRound extends GameInput

// =========================================================
// Optional controls (hooks, currently no logic behind them)
// =========================================================

case object Undo extends GameInput
case object Redo extends GameInput
