package de.htwg.wizard.model

import de.htwg.wizard.util.Observable

/**
 * GameState is a Snapshot of the game
 */

case class GameState(
                    amountOfPlayers: Int,
                    players: List[Player],
                    deck: Deck,
                    currentRound: Int,
                    totalRounds: Int,
                    currentTrump: CardColor,
                    currentStitch: Option[Stitch] = None
                    ) extends Observable

case class Stitch(played: Map[Int, Card])

