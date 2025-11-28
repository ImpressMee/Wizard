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
                    ) extends Observable:

  def createMemento(): GameStateMemento =
    GameStateMemento(
      amountOfPlayers,
      players,
      deck,
      currentRound,
      totalRounds,
      currentTrump,
      currentTrick
    )

  def restore(m: GameStateMemento): GameState =
    this.copy(
      amountOfPlayers = m.amountOfPlayers,
      players = m.players,
      deck = m.deck,
      currentRound = m.currentRound,
      totalRounds = m.totalRounds,
      currentTrump = m.currentTrump,
      currentTrick = m.currentTrick
    )
