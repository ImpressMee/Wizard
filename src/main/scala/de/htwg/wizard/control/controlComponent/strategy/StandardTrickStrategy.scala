package de.htwg.wizard.control.controlComponent.strategy

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

    if isWizard(card) || isJoker(card) then
      true

    else if trick.played.isEmpty then
      true

    else
      val leadColorOpt =
        trick.played.values.collectFirst {
          case c if isNormal(c) => c.color
        }

      leadColorOpt match
        case None =>
          true

        case Some(leadColor) =>
          if player.hand.exists(c => isNormal(c) && c.color == leadColor) then
            isNormal(card) && card.color == leadColor
          else
            true


}
