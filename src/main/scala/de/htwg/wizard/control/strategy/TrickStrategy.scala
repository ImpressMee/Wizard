package de.htwg.wizard.control.strategy

import de.htwg.wizard.model.*

/**
 * Strategy interface for determining trick rules.
 */
trait TrickStrategy {

  /** Determines the winner of a trick. */
  def winner(trick: Trick, trump: Option[CardColor]): (Int, Card)

  /** Validates whether a move is allowed. */
  def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean
}
