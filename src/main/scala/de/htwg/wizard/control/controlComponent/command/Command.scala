package de.htwg.wizard.control.controlComponent.command

import de.htwg.wizard.model.modelComponent.GameState

trait Command:
  def execute(state: GameState): GameState
