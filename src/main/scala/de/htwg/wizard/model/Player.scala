package de.htwg.wizard.model

/**
 * List() generates an empty list
*/

case class Player(
                   id: Int,
                   hand: List[Card]=List(),
                   tricks: Int=0,
                   totalPoints: Int=0,
                   predictedTricks: Int=0
                 )
