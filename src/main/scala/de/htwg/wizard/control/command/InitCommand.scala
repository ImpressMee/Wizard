package de.htwg.wizard.control.command

import de.htwg.wizard.model.*

case class InitCommand(playerCount: Int) extends Command:
  def execute(state: GameState): GameState =
    GameState(
      amountOfPlayers = playerCount,
      players = (0 until playerCount).map(Player(_)).toList,
      deck = Deck().shuffle(),
      currentRound = 0,
      totalRounds = Array(4,3,2,2)(playerCount - 3)
    )
