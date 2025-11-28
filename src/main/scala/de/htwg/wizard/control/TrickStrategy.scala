package de.htwg.wizard.control

import de.htwg.wizard.model.*

trait TrickStrategy:
  def winner(stitch: Trick, trump: Option[CardColor]): (Int, Card)
