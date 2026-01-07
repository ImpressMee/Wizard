package de.htwg.wizard.control.strategy

import de.htwg.wizard.model.*

trait TrickStrategy:
  def winner(stitch: Trick, trump: Option[CardColor]): (Int, Card)
  def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean
