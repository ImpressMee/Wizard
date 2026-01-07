package de.htwg.wizard.control

import de.htwg.wizard.control.command.{InitCommand, PrepareRoundCommand}
import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import de.htwg.wizard.control.strategy.{StandardTrickStrategy, TrickStrategy}

import scala.util.{Failure, Success, Try}
import scala.util.boundary
import scala.util.boundary.break
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.observer.Observer

class GameControl(
                   val strategy: TrickStrategy = StandardTrickStrategy()
                 ) {

  // ============================================================
  // OBSERVER REGISTRY (Solution B)
  // ============================================================

  private var observers: List[Observer] = Nil

  def registerObservers(obs: Observer*): Unit =
    observers ++= obs

  private def attachObservers(gs: GameState): GameState =
    observers.foreach(gs.add)
    gs


  // ============================================================
  // MEMENTO (undo management)
  // ============================================================

  private var history: List[GameStateMemento] = Nil

  private[control] def saveState(gs: GameState): Unit =
    history = gs.createMemento() :: history

  def undo(current: GameState): GameState =
    history match
      case m :: rest =>
        history = rest
        current.restore(m)
      case Nil =>
        current


  // ============================================================
  // GUI / TUI ENTRY POINTS
  // ============================================================

  var currentState: Option[GameState] = None
  private var currentPhase: Option[GameStatePhase] = None


  def submitPlayerAmount(n: Int): Unit =
    currentState.foreach(saveState)

    val initCmd   = InitCommand(this, n)
    val afterInit = attachObservers(initCmd.execute())

    val prepCmd   = PrepareRoundCommand(this, afterInit)
    val afterPrep = attachObservers(prepCmd.execute())

    currentState = Some(afterPrep)
    currentPhase = Some(PredictState)
    step()


  def prepareNextRound(trump: Option[CardColor]): Unit =
    currentState.foreach(saveState)

    currentState = currentState.map { gs =>
      attachObservers(doPrepareNextRound(gs))
    }

    currentPhase =
      if currentState.exists(gs => gs.currentRound >= gs.totalRounds)
      then Some(FinishState)
      else Some(PredictState)

    step()


  def submitPredictions(predictions: Map[Int, Int]): Unit =
    currentState.foreach(saveState)
    currentState = currentState.map(gs =>
      attachObservers(doPredictTricks(gs, predictions))
    )
    currentPhase = Some(TrickState(1))
    step()



  def playTrick(trickNr: Int, moves: Map[Int, Int]): Unit =
    currentState.foreach(saveState)

    currentState = currentState.map { gs =>
      val before = gs.players.head.hand.size
      val next   = attachObservers(doPlayOneTrick(trickNr, gs, moves))
      val after  = next.players.head.hand.size

      currentPhase =
        if after == before then Some(TrickState(trickNr))
        else if after == 0 then Some(ScoreState)
        else Some(TrickState(trickNr + 1))

      next
    }

    step()


  // ============================================================
  // GAME LOOP
  // ============================================================

  def runGame(views: Observer*): Unit =
    val gs = attachObservers(
      GameState(
        amountOfPlayers = 0,
        players = Nil,
        deck = new Deck(),
        currentRound = 0,
        totalRounds = 0,
        currentTrump = None
      )
    )

    registerObservers(views*)
    currentState = Some(gs)
    currentPhase = Some(InitState)
    step()


  private def step(): Unit =
    (currentPhase, currentState) match
      case (Some(phase), Some(state)) =>
        val (nextPhase, nextState) = phase.run(this, state)
        currentState = Some(attachObservers(nextState))
        currentPhase = nextPhase
      case _ => ()

  // ============================================================
  // INITIALIZATION
  // ============================================================

  private[control] def doInitGame(playerCount: Int): GameState =
    val gs = GameState(
      amountOfPlayers = playerCount,
      players = (0 until playerCount).map(id => Player(id)).toList,
      deck = new Deck().shuffle(),
      currentRound = 0,
      totalRounds = Array(4, 3, 2, 2)(playerCount - 3),
      currentTrump = None
    )

    val withObs = attachObservers(gs)
    withObs.notifyObservers(StateChanged(withObs))
    withObs


  // ============================================================
  // PREPARE NEXT ROUND
  // ============================================================

  private[control] def doPrepareNextRound(gs: GameState): GameState =
    val newRound = gs.currentRound + 1

    val fullDeck = Deck().shuffle()

    val trumpColor =
      fullDeck.cards
        .dropWhile(card => isWizard(card) || isJoker(card))
        .headOption
        .map(_.color)

    val (playersWithHands, remainingDeck) =
      gs.players.foldLeft((List.empty[Player], fullDeck)) {
        case ((acc, deck), p) =>
          val (hand, nextDeck) = deck.deal(newRound)
          (acc :+ p.copy(hand = hand), nextDeck)
      }

    val newState = attachObservers(
      gs.copy(
        currentRound = newRound,
        players = playersWithHands,
        deck = remainingDeck,
        currentTrump = trumpColor
      )
    )

    newState.notifyObservers(RoundStarted(newRound, newState))
    newState


  // ============================================================
  // PREDICT TRICKS
  // ============================================================

  private[control] def doPredictTricks(
                                        gs: GameState,
                                        predictions: Map[Int, Int]
                                      ): GameState =

    val updatedPlayersOpt =
      gs.players.foldLeft(Option(List.empty[Player])) {
        case (None, _) => None
        case (Some(acc), player) =>
          predictions.get(player.id).filter(_ >= 0).map(pred =>
            acc :+ player.copy(predictedTricks = pred)
          )
      }

    if updatedPlayersOpt.isEmpty then
      gs.notifyObservers(StateChanged(gs))
      gs
    else
      val newState = attachObservers(
        gs.copy(players = updatedPlayersOpt.get)
      )
      newState.notifyObservers(StateChanged(newState))
      newState


  // ============================================================
  // PLAY ONE TRICK
  // ============================================================

  private[control] def doPlayOneTrick(
                                       trickNumber: Int,
                                       gs: GameState,
                                       moves: Map[Int, Int]
                                     ): GameState =

    if gs.players.exists(_.hand.isEmpty) then
      gs.notifyObservers(StateChanged(gs))
      gs
    else
      gs.notifyObservers(TrickStarted(trickNumber, gs))

      val collectedOpt =
        gs.players.foldLeft(Option(Trick(Map.empty[PlayerID, Card]))) {
          case (None, _) => None
          case (Some(trick), player) =>
            moves.get(player.id).flatMap { idx =>
              if idx < 0 || idx >= player.hand.size then None
              else
                val card = player.hand(idx)
                if !isAllowedMove(card, player, trick) then None
                else Some(Trick(trick.played + (player.id -> card)))
            }
        }

      if collectedOpt.isEmpty then
        gs.notifyObservers(StateChanged(gs))
        gs
      else
        val completedTrick = collectedOpt.get

        val afterRemoval = attachObservers(
          gs.copy(
            players = gs.players.map { p =>
              moves.get(p.id).map(idx => p.copy(hand = p.hand.patch(idx, Nil, 1))).getOrElse(p)
            },
            currentTrick = Some(completedTrick)
          )
        )

        afterRemoval.notifyObservers(StateChanged(afterRemoval))

        val (winnerId, _) =
          strategy.winner(completedTrick, afterRemoval.currentTrump)

        val finalState = attachObservers(
          afterRemoval.copy(
            players = afterRemoval.players.map { p =>
              if p.id == winnerId then p.copy(tricks = p.tricks + 1) else p
            },
            currentTrick = None
          )
        )

        finalState.notifyObservers(TrickFinished(winnerId, finalState))
        finalState


  // ============================================================
  // RULES
  // ============================================================

  private def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
    val leadOpt = trick.played.values.collectFirst {
      case c if isNormal(c) => c.color
    }

    leadOpt.forall { lead =>
      !player.hand.exists(c => isNormal(c) && c.color == lead) ||
        (isNormal(card) && card.color == lead)
    }


  // ============================================================
  // SCORE ROUND
  // ============================================================

  private[control] def doScoreRound(gs: GameState): GameState =
    val scored = attachObservers(
      gs.copy(
        players = gs.players.map(p =>
          p.copy(totalPoints = p.totalPoints + calculateRoundPoints(p))
        )
      )
    )

    scored.notifyObservers(RoundFinished(scored))

    val finalState = attachObservers(
      scored.copy(
        players = scored.players.map(p =>
          p.copy(tricks = 0, predictedTricks = 0)
        )
      )
    )

    finalState.notifyObservers(StateChanged(finalState))
    finalState


  private def calculateRoundPoints(p: Player): Int =
    if p.predictedTricks == p.tricks then 20 + p.tricks * 10
    else p.tricks * 10 - 10 * (p.tricks - p.predictedTricks).abs


  // ============================================================
  // DETERMINE WINNER
  // ============================================================

  private[control] def doDetermineWinner(gs: GameState): GameState =
    val withObs = attachObservers(gs)
    withObs.notifyObservers(GameFinished(gs.players.maxBy(_.totalPoints), withObs))
    withObs
}
