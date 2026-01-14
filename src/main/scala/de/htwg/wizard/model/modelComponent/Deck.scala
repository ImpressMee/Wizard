package de.htwg.wizard.model.modelComponent

import scala.util.Random

case class Deck(cards: List[Card]=List()):
  private val colors = CardColor.values.toVector
  private val values = 1 to 3

  // returns new Deck with random shuffled cards
  def shuffle(): Deck =
    Deck(Random.shuffle(cards))

  // returns a new Hand with size n for a player 
  // and a new Deck with the rest of the Cards
  def deal(handsize: Int): (List[Card], Deck) =
    val hand = cards.take(handsize)
    val rest = cards.drop(handsize)
    (hand, Deck(rest))
    
//The companion object Deck can provide factory methods
object Deck:
  // apply() seems to be like a Constructor, 
  // but it is a normal method, that builds a new Object
  def apply(): Deck =
    val colors = CardColor.values.toVector
    val values = 1 to 13

    val normalCards =
      for color <- colors; v <- values
        yield Card(color, v)

    val wizards =
      for color <- colors
        yield Card(color,"wizard")

    val jokers  =
      for color <- colors
        yield Card(color,"joker")
    Deck((normalCards ++ wizards ++ jokers).toList)

// When we call Deck(List[Card]), 
// it is not overwritten by the code in apply()
// instead, it is handled by the case class, 
// because the custom apply method takes no parameters.
    
// The case class automatically generates 
// an additional apply method with parameters
