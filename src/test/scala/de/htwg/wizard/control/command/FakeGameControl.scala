package de.htwg.wizard.control.command

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*

class FakeGameControl extends GameControl {

  var lastCalled: String = ""
  var lastArgs: Any = _
  private val dummyState = GameState(0, Nil, Deck(), 0, 0)

  override def initGame(playerCount: Int): GameState =
    lastCalled = "initGame"
    lastArgs = playerCount
    dummyState

  override def prepareNextRound(state: GameState): GameState =
    lastCalled = "prepareNextRound"
    lastArgs = state
    dummyState

  override def predictTricks(state: GameState, predictions: Map[Int, Int]): GameState =
    lastCalled = "predictTricks"
    lastArgs = (state, predictions)
    dummyState

  override def playOneTrick(
                             trickNr: Int,
                             state: GameState,
                             moves: Map[Int, Int]
                           ): GameState =
    lastCalled = "playOneTrick"
    lastArgs = (trickNr, state, moves)
    dummyState

  override def scoreRound(state: GameState): GameState =
    lastCalled = "scoreRound"
    lastArgs = state
    dummyState

  override def determineWinner(state: GameState): GameState =
    lastCalled = "determineWinner"
    lastArgs = state
    dummyState
}
