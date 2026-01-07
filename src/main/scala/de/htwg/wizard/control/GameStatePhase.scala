package de.htwg.wizard.control

import de.htwg.wizard.model.GameState

/**
 * State-Pattern:
 * Entscheidet ausschließlich den SPIELABLAUF.
 *
 * - KEINE Events
 * - KEINE Observer
 * - KEINE Logik
 * - KEINE Side-Effects
 */
trait GameStatePhase:

  /**
   * Liefert die nächste Phase basierend auf dem aktuellen GameState.
   */
  def next(state: GameState): GameStatePhase


// ============================================================
// INIT
// ============================================================

case object InitState extends GameStatePhase:

  override def next(state: GameState): GameStatePhase =
    // wartet auf Spieleranzahl → danach Prediction
    PredictState


// ============================================================
// PREPARE NEXT ROUND
// ============================================================

case object PrepareRoundState extends GameStatePhase:

  override def next(state: GameState): GameStatePhase =
    // nach Vorbereitung wird vorhergesagt
    PredictState


// ============================================================
// PREDICT TRICKS
// ============================================================

case object PredictState extends GameStatePhase:

  override def next(state: GameState): GameStatePhase =
    TrickState(1)


// ============================================================
// TRICK STATE (STATEFUL)
// ============================================================

case class TrickState(trickNr: Int) extends GameStatePhase:

  override def next(state: GameState): GameStatePhase =
    // solange noch Karten auf der Hand sind → nächster Stich
    if state.players.headOption.exists(_.hand.nonEmpty) then
      TrickState(trickNr + 1)
    else
      // keine Karten mehr → Runde werten
      ScoreState


// ============================================================
// SCORE ROUND
// ============================================================

case object ScoreState extends GameStatePhase:

  override def next(state: GameState): GameStatePhase =
    // letzte Runde erreicht?
    if state.currentRound >= state.totalRounds then
      FinishState
    else
      PrepareRoundState


// ============================================================
// FINISH GAME
// ============================================================

case object FinishState extends GameStatePhase:

  override def next(state: GameState): GameStatePhase =
    // Endzustand, bleibt hier
    this
