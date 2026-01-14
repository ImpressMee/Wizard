package de.htwg.wizard.model.modelComponent

import de.htwg.wizard.model.modelComponent.CardColor


/**
 * GameState is a Snapshot of the game
 */
case class GameState(
                      amountOfPlayers: Int,
                      players: List[Player],
                      deck: Deck,
                      currentRound: Int,
                      totalRounds: Int,
                      currentTrump: Option[CardColor] = None,
                      currentTrick: Option[Trick] = None,
                      completedTricks: Int = 0
                    ):

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
object GameState {
  def empty: GameState =
    GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = Deck(),
      currentRound = 0,
      totalRounds = 0,
      currentTrick = None,
      currentTrump = None
    )
}
  
