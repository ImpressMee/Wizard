package de.htwg.wizard.control.controlComponent.strategy

import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Player, Trick}

/**
 * Strategy interface for determining trick rules.
 */
trait TrickStrategy {

  /** Determines the winner of a trick. */
  def winner(trick: Trick, trump: Option[CardColor]): (Int, Card)

  /** Validates whether a move is allowed. */
  def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean
}
