package de.htwg.wizard.control.strategy

import de.htwg.wizard.model.*

class AlternativeTrickStrategy extends TrickStrategy:

  override def winner(stitch: Trick, trump: Option[CardColor]): (Int, Card) =
    val cards = stitch.played.toList

    // Wizard wins instantly (first wizard)
    cards.find((_, c) => isWizard(c)) match
      case Some(w) => return w
      case None    => ()

    // Joker handling: first loses → last wins
    val jokerCards = cards.filter((_, c) => isJoker(c))
    if jokerCards.nonEmpty then
      return jokerCards.last   // last joker wins

    // Highest trump wins (only normal cards)
    val trumpCards =
      cards.filter((_, c) => isNormal(c) && trump.contains(c.color))


    if trumpCards.nonEmpty then
      return trumpCards.maxBy((_, c) => value(c))

    // Highest normal card wins (color fully ignored)
    val normalCards = cards.filter((_, c) => isNormal(c))
    if normalCards.nonEmpty then
      return normalCards.maxBy((_, c) => value(c))

    // Fallback (should never occur)
    cards.last
