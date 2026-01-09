package de.htwg.wizard.control

import de.htwg.wizard.control.command.*
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.observer.{Observable, Observer}
import de.htwg.wizard.control.strategy.{StandardTrickStrategy, TrickStrategy}
import de.htwg.wizard.model.*

class GameControl(
                   val strategy: TrickStrategy = StandardTrickStrategy()
                 ) extends Observable {

  // =========================================================
  // STATE + MEMENTO
  // =========================================================

  private var currentState: Option[GameState] = None
  private var currentPhase: GameStatePhase = InitState
  private var history: List[GameStateMemento] = Nil

  private def saveState(gs: GameState): Unit =
    history ::= gs.createMemento()

  def undo(): Unit =
    (history, currentState) match
      case (m :: rest, Some(gs)) =>
        history = rest
        currentState = Some(gs.restore(m))
        notifyObservers(StateChanged(currentState.get))
        firePhaseEvent()
      case _ => ()

  // =========================================================
  // OBSERVER
  // =========================================================

  def registerObservers(obs: Observer*): Unit =
    obs.foreach(add)

  // =========================================================
  // ENTRY
  // =========================================================

  def runGame(obs: Observer*): Unit =
    registerObservers(obs*)

    val gs = GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = Deck(),
      currentRound = 0,
      totalRounds = 0,
      currentTrump = None,
      currentTrick = None,
      completedTricks = 0
    )


    currentState = Some(gs)
    currentPhase = InitState

    notifyObservers(StateChanged(gs))
    firePhaseEvent()

  // =========================================================
  // PUBLIC API (für Commands)
  // =========================================================

  def initGame(playerCount: Int): GameState =
    doInitGame(playerCount)

  def prepareNextRound(gs: GameState): GameState =
    doPrepareNextRound(gs)

  def predictTricks(gs: GameState, predictions: Map[Int, Int]): GameState =
    doPredictTricks(gs, predictions)

  def playOneTrick(
                    trickNr: Int,
                    gs: GameState,
                    moves: Map[Int, Int]
                  ): GameState =
    doPlayOneTrick(trickNr, gs, moves)

  def scoreRound(gs: GameState): GameState =
    doScoreRound(gs)

  def determineWinner(gs: GameState): GameState =
    doDetermineWinner(gs)

  // =========================================================
  // VIEW → CONTROLLER
  // =========================================================

  def submitPlayerAmount(n: Int): Unit =
    currentState.foreach { gs =>
      saveState(gs)

      val afterInit = InitCommand(this, n).execute()
      val afterPrep = PrepareRoundCommand(this, afterInit).execute()

      currentState = Some(afterPrep)
      currentPhase = currentPhase.next(afterPrep)

      notifyObservers(StateChanged(afterPrep))
      firePhaseEvent()
    }

  def submitPredictions(predictions: Map[Int, Int]): Unit =
    currentState.foreach { gs =>
      saveState(gs)

      val next = PredictCommand(this, gs, predictions).execute()
      currentState = Some(next)
      currentPhase = currentPhase.next(next)

      notifyObservers(StateChanged(next))
      firePhaseEvent()
    }

  def playTrick(trickNr: Int, moves: Map[Int, Int]): Unit =
    currentState.foreach { gs =>
      saveState(gs)

      val afterTrick =
        PlayTrickCommand(this, trickNr, gs, moves).execute()

      currentState = Some(afterTrick)

      notifyObservers(StateChanged(afterTrick))

      if afterTrick.players.head.hand.isEmpty then
        finishRound()
      else
        currentPhase = TrickState(afterTrick.completedTricks + 1)
        firePhaseEvent()
    }


  // =========================================================
  // INTERNAL FLOW
  // =========================================================

  private def finishRound(): Unit =
    currentState.foreach { gs =>
      saveState(gs)

      // 1. Runde werten
      val scored = ScoreRoundCommand(this, gs).execute()
      currentState = Some(scored)

      // 2. RUNDENÜBERSICHT anzeigen
      notifyObservers(RoundFinished(scored))

      // 3. Phase weiter
      currentPhase = currentPhase.next(scored)
    }


  private def finishGame(): Unit =
    currentState.foreach { gs =>
      DetermineWinnerCommand(this, gs).execute()
      notifyObservers(GameFinished(gs.players.maxBy(_.totalPoints), gs))
    }

  // =========================================================
  // PHASE → EVENT MAPPING
  // =========================================================

  private def firePhaseEvent(): Unit =
    (currentPhase, currentState) match
      case (InitState, Some(gs)) =>
        notifyObservers(PlayerAmountRequested(gs))

      case (PredictState, Some(gs)) =>
        notifyObservers(PredictionsRequested(gs))

      case (TrickState(n), Some(gs)) =>
        notifyObservers(TrickMoveRequested(n, gs))

      case _ => ()

  // =========================================================
  // INTERNAL GAME LOGIC (NUR CONTROLLER)
  // =========================================================

  private[control] def doInitGame(playerCount: Int): GameState =
    GameState(
      amountOfPlayers = playerCount,
      players = (0 until playerCount).map(Player(_)).toList,
      deck = Deck().shuffle(),
      currentRound = 0,
      totalRounds = Array(4, 3, 2, 2)(playerCount - 3),
      currentTrump = None,
      currentTrick = None
    )

  private[control] def doPrepareNextRound(gs: GameState): GameState =
    val newRound = gs.currentRound + 1
    val deck = Deck().shuffle()

    val trump =
      deck.cards
        .dropWhile(c => isWizard(c) || isJoker(c))
        .headOption
        .map(_.color)

    val (players, restDeck) =
      gs.players.foldLeft((List.empty[Player], deck)) {
        case ((acc, d), p) =>
          val (hand, nd) = d.deal(newRound)
          (acc :+ p.copy(
            hand = hand,
            tricks = 0,
            predictedTricks = 0
          ), nd)

      }

    gs.copy(
      currentRound = newRound,
      players = players,
      deck = restDeck,
      currentTrump = trump,
      currentTrick = None,
      completedTricks = 0
    )

  private[control] def doPredictTricks(
                                        gs: GameState,
                                        predictions: Map[Int, Int]
                                      ): GameState =
    gs.copy(
      players = gs.players.map(p =>
        p.copy(predictedTricks = predictions.getOrElse(p.id, 0))
      )
    )

  private[control] def doPlayOneTrick(
                                       trickNr: Int,
                                       gs: GameState,
                                       moves: Map[Int, Int]
                                     ): GameState = {

    val trick =
      Trick(moves.map { case (pid, idx) =>
        pid -> gs.players.find(_.id == pid).get.hand(idx)
      })

    val (winnerId, _) =
      strategy.winner(trick, gs.currentTrump)

    val updatedPlayers =
      gs.players.map { p =>
        val newHand =
          moves.get(p.id).map(i => p.hand.patch(i, Nil, 1)).getOrElse(p.hand)
        val tricks = if p.id == winnerId then p.tricks + 1 else p.tricks
        p.copy(hand = newHand, tricks = tricks)
      }

    gs.copy(
      players = updatedPlayers,
      currentTrick = Some(trick)
    )
  }

  private[control] def doScoreRound(gs: GameState): GameState =
    gs.copy(
      players = gs.players.map { p =>
        val points =
          if p.tricks == p.predictedTricks then 20 + p.tricks * 10
          else p.tricks * 10 - 10 * (p.tricks - p.predictedTricks).abs

        p.copy(
          totalPoints = p.totalPoints + points
        )
      },
      currentTrick = None
    )


  private[control] def doDetermineWinner(gs: GameState): GameState = gs

  def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
    strategy.isAllowedMove(card, player, trick)

  def continueAfterRound(): Unit =
    currentState.foreach { gs =>
      currentPhase match
        case FinishState =>
          // Spiel ist vorbei → Endscreen anzeigen
          DetermineWinnerCommand(this, gs).execute()
          notifyObservers(GameFinished(gs.players.maxBy(_.totalPoints), gs))

        case _ =>
          // nächste Runde vorbereiten
          val prepared = PrepareRoundCommand(this, gs).execute()
          currentState = Some(prepared)
          currentPhase = currentPhase.next(prepared)

          notifyObservers(StateChanged(prepared))
          firePhaseEvent()
    }


}
