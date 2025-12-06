package de.htwg.wizard.control.strategy

import de.htwg.wizard.model.*

trait TrickStrategy:
  def winner(stitch: Trick, trump: Option[CardColor]): (Int, Card)
