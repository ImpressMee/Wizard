package de.htwg.wizard.persistence

import de.htwg.wizard.model.modelComponent.GameState

trait FileIO {
  def save(state: GameState): Unit
  def load(): GameState
  def hasSave: Boolean
}

