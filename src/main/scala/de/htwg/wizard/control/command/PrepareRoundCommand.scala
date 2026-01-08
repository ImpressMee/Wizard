package de.htwg.wizard.control.command

import de.htwg.wizard.model.*

object PrepareRoundCommand extends Command:

  def execute(state: GameState): GameState =
    val newRound = state.currentRound + 1
    val deck = Deck().shuffle()

    val (players, restDeck) =
      state.players.foldLeft((List.empty[Player], deck)) {
        case ((acc, d), p) =>
          val (hand, nd) = d.deal(newRound)
          (acc :+ p.copy(hand = hand), nd)
      }

    state.copy(
      currentRound = newRound,
      players = players,
      deck = restDeck,
      currentTrump = None
    )
