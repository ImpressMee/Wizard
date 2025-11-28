package de.htwg.wizard.model


import de.htwg.wizard.control.Observable

/**
 * GameState is a Snapshot of the game
 */

case class GameState(
                      amountOfPlayers: Int,
                      players: List[Player],
                      deck: Deck,
                      currentRound: Int,
                      totalRounds: Int,
                      currentTrump: Option[CardColor],
                      currentTrick: Option[Trick] = None
                    ) extends Observable
// extends Observable makes GameState observable, 
// allowing other parts of the program to automatically 
// react to changes.



