
package de.htwg.wizard.control.controlComponent.component

import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponent.GameControl
import de.htwg.wizard.model.modelComponent.{GameState, PlayerID}
import jakarta.inject.Inject

class GameComponent @Inject() (
                                control: GameControl
                              ) extends GamePort {

  override def init(): Unit =
    control.init()

  override def registerObserver(observer: Observer): Unit =
    control.registerObserver(observer)

  override def startGame(): Unit =
    control.start(GameState.empty)

  override def handleInput(input: GameInput): Unit =
    input match
      case PlayerAmountSelected(n) => control.submitPlayerAmount(n)
      case PredictionsSubmitted(p) => control.submitPredictions(p)
      case TrickMovesSubmitted(m)  => control.playTrick(m)
      case ContinueAfterRound      => control.prepareNextRound()
      case Undo                    => control.undo()
      case Redo                    => control.redo()
      case LoadGame                => control.loadGame()
      case _                      => ()

  override def isAllowedMove(
                              playerId: PlayerID,
                              cardIndex: Int,
                              state: GameState
                            ): Boolean =
    control.isAllowedMove(playerId, cardIndex, state)

  override def canSafelyExit: Boolean =
    control.canSafelyExit
}
  

