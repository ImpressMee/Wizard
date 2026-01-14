package de.htwg.wizard.control.controlComponent.command

import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Deck, GameState, Player}

case class InitCommand(playerCount: Int) extends Command:

  val roundsByPlayers = Array(4, 3, 2, 2)
  val idx = math.max(0, math.min(playerCount - 3, roundsByPlayers.length - 1))

  def execute(state: GameState): GameState =
    GameState(
      amountOfPlayers = playerCount,
      players = (0 until playerCount).map(Player(_)).toList,
      deck = Deck().shuffle(),
      currentRound = 0,
      totalRounds =  roundsByPlayers(idx)
    )
