package de.htwg.wizard.control.controlComponents.strategy

import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Player, Trick, isJoker, isNormal, isWizard, value}

/**
 * Default Wizard trick evaluation strategy.
 */
class StandardTrickStrategy extends TrickStrategy {

  override def winner(trick: Trick, trump: Option[CardColor]): (Int, Card) = {
    val cards = trick.played.toList

    cards.collectFirst { case (pid, card) if isWizard(card) => (pid, card) } match
      case Some(w) => w
      case None =>

        if cards.forall((_, c) => isJoker(c)) then
          cards.last
        else
          val leadColor =
            cards.collectFirst { case (_, c) if isNormal(c) => c.color }

          leadColor match
            case None => cards.last
            case Some(color) =>
              val trumpCards =
                cards.filter((_, c) => isNormal(c) && trump.contains(c.color))

              if trumpCards.nonEmpty then
                trumpCards.maxBy((_, c) => value(c))
              else
                cards
                  .filter((_, c) => isNormal(c) && c.color == color)
                  .maxBy((_, c) => value(c))
  }

  override def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
    if trick.played.isEmpty then true
    else
      val lead =
        trick.played.values.collectFirst { case c if isNormal(c) => c.color }

      lead.forall { c =>
        !player.hand.exists(h => isNormal(h) && h.color == c) ||
          (isNormal(card) && card.color == c)
      }
}
