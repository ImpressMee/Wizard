package de.htwg.wizard.model

import scala.util.Random

enum CardColor:
  case Red, Green, Blue, Yellow

case class Card(color: CardColor, value: Int)

def trumpColor(): CardColor=
  CardColor.values(Random.nextInt(CardColor.values.length))
