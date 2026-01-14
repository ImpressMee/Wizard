package de.htwg.wizard.control.controlComponents.command

import de.htwg.wizard.model.modelComponent.GameState

trait Command:
  def execute(state: GameState): GameState
