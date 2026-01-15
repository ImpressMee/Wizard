package de.htwg.wizard.control.controlComponent.command

import de.htwg.wizard.control.controlComponent.command.Command
import de.htwg.wizard.model.modelComponent.{CardType, Deck, GameState, Player}

object PrepareRoundCommand extends Command {

  def execute(state: GameState): GameState = {

    val newRound = state.currentRound + 1
    val deck = Deck().shuffle()

    val trump =
      deck.cards
        .dropWhile(c =>
          c.cardType == CardType.Wizard ||
            c.cardType == CardType.Joker
        )
        .headOption
        .map(_.color)

    val (players, restDeck) =
      state.players.foldLeft((List.empty[Player], deck)) {
        case ((acc, d), p) =>
          val (hand, nd) = d.deal(newRound)
          (
            acc :+ p.copy(
              hand            = hand,
              tricks          = 0,
              predictedTricks = 0
            ),
            nd
          )
      }

    state.copy(
      currentRound     = newRound,
      players          = players,
      deck             = restDeck,
      currentTrump     = trump,
      currentTrick     = None,
      completedTricks  = 0
    )
  }
}
