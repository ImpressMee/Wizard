package de.htwg.wizard.model

/**
 * List() generates an empty list
*/

case class Player(
                 id: Int,
                 hand: List[Card] = List(),
                 stitches: Int = 0,
                 totalPoints: Int = 0,
                 predictedStitches: Int = 0
                 )
