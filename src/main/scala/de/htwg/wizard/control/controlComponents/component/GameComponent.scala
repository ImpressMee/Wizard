
package de.htwg.wizard.control.controlComponents.component

import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponents.GameControl
import de.htwg.wizard.control.controlComponents.strategy.{StandardTrickStrategy, TrickStrategy}
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Deck, GameState, ModelComponent, PlayerID}


class GameComponent(
                     model: ModelInterface,
                     strategy: TrickStrategy = StandardTrickStrategy()
                   ) extends GamePort {

  private var observers: List[Observer] = Nil

  private def notifyObservers(event: GameEvent): Unit =
    observers.foreach(_.update(event))

  override def registerObserver(observer: Observer): Unit =
    observers ::= observer

  private val control = new GameControl(model, strategy, notifyObservers)

  override def startGame(): Unit =
    control.start(
      GameState(
        amountOfPlayers = 0,
        players = Nil,
        deck = Deck(),
        currentRound = 0,
        totalRounds = 0,
        currentTrick = None,
        currentTrump = None
      )
    )

  override def handleInput(input: GameInput): Unit =
    input match
      case PlayerAmountSelected(n)        => control.submitPlayerAmount(n)
      case PredictionsSubmitted(p)        => control.submitPredictions(p)
      case TrickMovesSubmitted(m)         => control.playTrick(m)
      case ContinueAfterRound             => control.prepareNextRound()
      case Undo                           => control.undo()
      case Redo                           => control.redo()
      case _                              => ()

  override def isAllowedMove(
                              playerId: PlayerID,
                              cardIndex: Int,
                              state: GameState
                            ): Boolean =
    control.isAllowedMove(playerId, cardIndex, state)
}
