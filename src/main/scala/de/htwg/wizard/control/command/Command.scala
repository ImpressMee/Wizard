package de.htwg.wizard.control.command

import de.htwg.wizard.model.GameState

trait Command:
  def execute(): GameState
