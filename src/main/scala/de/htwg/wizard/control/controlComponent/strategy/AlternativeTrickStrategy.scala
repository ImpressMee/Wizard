package de.htwg.wizard.control.controlComponent.strategy

import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Player, Trick, isJoker, isNormal, isWizard, value}

class AlternativeTrickStrategy extends TrickStrategy:

  override def winner(trick: Trick, trump: Option[CardColor]): (Int, Card) = {
    val cards = trick.played.toList

    // 1. Wizard wins immediately (first wizard played)
    cards.collectFirst { case (pid, card) if isWizard(card) => (pid, card) } match
      case Some(winner) => winner
      case None =>

        // 2. Joker rule: first Joker loses, last Joker wins
        val jokerCards =
          cards.filter((_, card) => isJoker(card))

        if jokerCards.nonEmpty then
          jokerCards.last
        else
          // 3. Highest trump card wins (normal cards only)
          val trumpCards =
            cards.filter((_, card) => isNormal(card) && trump.contains(card.color))

          if trumpCards.nonEmpty then
            trumpCards.maxBy((_, card) => value(card))
          else
            // 4. Highest normal card wins (color ignored)
            val normalCards =
              cards.filter((_, card) => isNormal(card))

            if normalCards.nonEmpty then
              normalCards.maxBy((_, card) => value(card))
            else
              // Defensive fallback (should never occur)
              cards.last
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


