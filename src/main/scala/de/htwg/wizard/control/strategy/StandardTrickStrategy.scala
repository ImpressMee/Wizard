package de.htwg.wizard.control.strategy

import de.htwg.wizard.control.strategy.TrickStrategy
import de.htwg.wizard.model.*

class StandardTrickStrategy extends TrickStrategy:

  override def winner(stitch: Trick, trump: Option[CardColor]): (Int, Card) =
    val cards = stitch.played.toList

    // Wizard wins instantly (first wizard)
    cards.find((_, c) => isWizard(c)) match
      case Some(w) => return w
      case None    => ()

    // If all cards are JOKER → LAST joker wins
    if cards.forall((_, c) => isJoker(c)) then
      return cards.last

    // Determine lead color (first NORMAL card)
    val leadColorOpt = {
      cards.collectFirst{case (_, c) if isNormal(c) => c.color }
    }

    // If NO normal card exists => only Jokers & Wizards played
    // (Wizard already handled that means only jokers here)
    if leadColorOpt.isEmpty then
      return cards.last // last joker winner

    val leadColor = leadColorOpt.get

    // Trump cards (normal trump cards only)
    val trumpCards = cards.filter((_, c) => isNormal(c) && trump.contains(c.color))
    if trumpCards.nonEmpty then
      return trumpCards.maxBy((_, c) => value(c))

    // Follow (normal cards only)
    val follow = cards.filter((_, c) => isNormal(c) && c.color == leadColor)
    if follow.nonEmpty then
      return follow.maxBy((_, c) => value(c))

    // Fallback (no normals? last joker)
    cards.last

