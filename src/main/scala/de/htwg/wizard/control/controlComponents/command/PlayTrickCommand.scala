package de.htwg.wizard.control.controlComponents.command

import de.htwg.wizard.control.controlComponents.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{GameState, Trick}

case class PlayTrickCommand(
                             moves: Map[Int, Int],
                             strategy: TrickStrategy
                           ) extends Command:

  override def execute(state: GameState): GameState =
    val trick = Trick(
      state.players.map { p =>
        p.id -> p.hand(moves(p.id))
      }.toMap
    )

    val (winnerId, _) =
      strategy.winner(trick, state.currentTrump)

    val updatedPlayers =
      state.players.map { p =>
        val newHand =
          moves.get(p.id)
            .map(idx => p.hand.patch(idx, Nil, 1))
            .getOrElse(p.hand)

        val tricks =
          if p.id == winnerId then p.tricks + 1 else p.tricks

        p.copy(hand = newHand, tricks = tricks)
      }

    state.copy(
      players = updatedPlayers,
      currentTrick = None,
      completedTricks = state.completedTricks + 1
    )
