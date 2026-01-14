package de.htwg.wizard.control.controlComponent

import de.htwg.wizard.model.modelComponent.GameState

trait GameStatePhase {
  def next(state: GameState): GameStatePhase
}

