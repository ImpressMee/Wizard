package de.htwg.wizard.control.strategy

import de.htwg.wizard.model.*

class StandardTrickStrategy extends TrickStrategy:

  override def winner(trick: Trick, trump: Option[CardColor]): (Int, Card) = {
    val cards = trick.played.toList

    // 1. Wizard wins immediately (first wizard played)
    cards.collectFirst { case (pid, card) if isWizard(card) => (pid, card) } match
      case Some(winner) => winner
      case None =>

        // 2. All cards are Jokers → last Joker wins
        if cards.forall((_, card) => isJoker(card)) then
          cards.last
        else
          // 3. Determine lead color (first normal card)
          val leadColorOpt =
            cards.collectFirst { case (_, card) if isNormal(card) => card.color }

          leadColorOpt match
            case None =>
              // Only Jokers were played (Wizard already excluded)
              cards.last

            case Some(leadColor) =>
              // 4. Trump cards win over all others (normal cards only)
              val trumpCards =
                cards.filter((_, card) => isNormal(card) && trump.contains(card.color))

              if trumpCards.nonEmpty then
                trumpCards.maxBy((_, card) => value(card))
              else
                // 5. Highest card of the lead color wins
                val followCards =
                  cards.filter((_, card) => isNormal(card) && card.color == leadColor)

                if followCards.nonEmpty then
                  followCards.maxBy((_, card) => value(card))
                else
                  // Defensive fallback (should never occur)
                  cards.last
  }

  override def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean = {

    // First player in the trick may play any card
    if trick.played.isEmpty then
      true
    else
      // Determine lead color (first normal card played)
      val leadColorOpt =
        trick.played.values.collectFirst {
          case c if isNormal(c) => c.color
        }

      leadColorOpt match
        case None =>
          // Wizard or Joker led → no color obligation
          true

        case Some(leadColor) =>
          // Player has no card of the lead color → free choice
          if !player.hand.exists(c => isNormal(c) && c.color == leadColor) then
            true
          else
            // Player must follow suit
            isNormal(card) && card.color == leadColor
  
}
