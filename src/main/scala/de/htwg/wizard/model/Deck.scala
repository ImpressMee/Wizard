package de.htwg.wizard.model

import scala.util.Random

class Deck:
  private val colors = CardColor.values.toVector
  private val values = 1 to 3   // testweise: nur 1, 2, 3

  // generate 12 Cardobjects. 3 values × 4 colors
  private var cards: List[Card] =
    (for c <- colors; v <- values yield Card(c, v)).toList

  def shuffle(): Unit =
    cards = Random.shuffle(cards)

  def deal(n: Int): List[Card] =
    val hand = cards.take(n)
    cards = cards.drop(n)
    hand
