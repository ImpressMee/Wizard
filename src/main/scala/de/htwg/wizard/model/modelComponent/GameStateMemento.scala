package de.htwg.wizard.model.modelComponent

import de.htwg.wizard.model.modelComponent.CardColor

case class GameStateMemento(
                             amountOfPlayers: Int,
                             players: List[Player],
                             deck: Deck,
                             currentRound: Int,
                             totalRounds: Int,
                             currentTrump: Option[CardColor],
                             currentTrick: Option[Trick]
                           )



/*
1. GameState = Originator
   GameState contains the complete current game state
   (players, cards, round, trump, etc.).
   It can:

   - createMemento(): create a snapshot object,
   - restore(memento): fully restore a previously saved state.

2. GameStateMemento = Memento
   GameStateMemento is an immutable copy of all important parts
   of the game state. It contains data only—no logic.

3. GameControl = Caretaker
   The GameController manages a history list of mementos.

Process:

- Before any change to the GameState, the controller calls saveState(gs).
  This pushes a GameStateMemento onto the history list (stack behavior).

- An undo call (undo(current)) takes the top memento from the list
  and restores the previous state using current.restore(m).

- If the history is empty, nothing happens.
*/
