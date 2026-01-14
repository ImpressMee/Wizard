package de.htwg.wizard.control.controlComponents

import de.htwg.wizard.model.modelComponent.GameState

trait GameStatePhase {
  def next(state: GameState): GameStatePhase
}

