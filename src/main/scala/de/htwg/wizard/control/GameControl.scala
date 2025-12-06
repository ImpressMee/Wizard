package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import de.htwg.wizard.control.strategy.{StandardTrickStrategy, TrickStrategy}

import scala.util.{Try, Success, Failure}
import scala.util.boundary
import scala.util.boundary.break

class GameControl(val view: GameView, val strategy: TrickStrategy = StandardTrickStrategy()) {

  // ============================================================
  // MEMENTO CARETAKER
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
  // GAME LOOP
  // ============================================================

  def runGame(): Unit =
    var phase: Option[GameStatePhase] = Some(InitState)
    var state: GameState = GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = new Deck(),
      currentRound = 0,
      totalRounds = 0,
      currentTrump = None
    )
  
    while phase.isDefined do
      val (nextPhase, nextState) = phase.get.run(this, state)
      phase = nextPhase
      state = nextState



  private[control] def doInitGame(): GameState =
    view.askPlayerAmount()

    Try(view.readPlayerAmount()) match
      case Success(playerCount) =>
        if playerCount < 3 || playerCount > 6 then
          view.showError("Wrong amount! Try again.")
          doInitGame()
        else
          val deck = new Deck().shuffle()
          val players = (0 until playerCount).map(id => Player(id, Nil)).toList
  
          val gs = GameState(
            amountOfPlayers = playerCount,
            players = players,
            deck = deck,
            currentRound = 0,
            totalRounds = Array(4, 3, 2, 2)(playerCount - 3),
            currentTrump = None
          )
  
          gs.add(view)
          gs.notifyObservers()
          gs
  
      case Failure(_) =>
        view.showError("Invalid entry! Try again.")
        doInitGame()
  


  private[control] def doPrepareNextRound(gs: GameState): GameState =
    if gs.currentRound >= gs.totalRounds then gs
    else
      val newRound = gs.currentRound + 1
      val deck = gs.deck.shuffle()

      val trumpCard = deck.cards.head
      val rest = deck.copy(cards = deck.cards.tail)

      val trump =
        if isWizard(trumpCard) then Some(view.chooseTrump())
        else if isJoker(trumpCard) then None
        else Some(trumpCard.color)

      view.showRoundInfo(newRound, trump, gs.amountOfPlayers)

      var d = rest
      val players = gs.players.map { p =>
        val (hand, newD) = d.deal(newRound)
        d = newD
        p.copy(hand = hand)
      }

      gs.copy(
        currentRound = newRound,
        players = players,
        deck = d,
        currentTrump = trump
      )


  private[control] def doPredictTricks(gs: GameState): GameState =
    val players = gs.players.map { p =>
      view.askHowManyTricks(p)
      val prediction = view.readPositiveInt()
      p.copy(predictedTricks = prediction)
    }
    gs.copy(players = players)


  private[control] def doPlayOneTrick(n: Int, gs: GameState): GameState =
    internalPlayOneTrick(n, gs)

  private def internalPlayOneTrick(trickNumber: Int, gs: GameState): GameState =
    boundary:
      if gs.players.exists(_.hand.isEmpty) then
        view.showError("No active stitch!")
        break(gs)

      view.showTrickStart(trickNumber)

      var trickSoFar = Trick(Map())

      val movesOpt = boundary:
        val collected = gs.players.map { p =>
          view.askPlayerCard(p)
          val idx = view.readIndex(p)

          if idx < 0 || idx >= p.hand.size then
            view.showError("Invalid index!")
            break(None)

          val card = p.hand(idx)

          if !isAllowedMove(card, p, trickSoFar) then
            view.showError("You must follow the start color!")
            break(None)

          trickSoFar = Trick(trickSoFar.played + (p.id -> card))
          (p, idx, card)
        }
        Some(collected)

      if movesOpt.isEmpty then return gs
      val moves = movesOpt.get

      val updatedPlayers =
        moves.map { (p, idx, _) =>
          p.copy(hand = p.hand.patch(idx, Nil, 1))
        }

      val playedPairs = moves.map { (p, _, card) =>
        p.id -> card
      }.toMap

      val trick = Trick(playedPairs)
      val afterTrick = gs.copy(players = updatedPlayers, currentTrick = Some(trick))
      afterTrick.notifyObservers()

      val (winnerId, winningCard) = strategy.winner(trick, afterTrick.currentTrump)

      val scoredPlayers =
        afterTrick.players.map { p =>
          if p.id == winnerId then p.copy(tricks = p.tricks + 1) else p
        }

      val winner = scoredPlayers.find(_.id == winnerId).get
      view.showTrickWinner(winner, winningCard)

      afterTrick.copy(players = scoredPlayers, currentTrick = None)


  private def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
    val leadOpt = trick.played.values.collectFirst {
      case c if isNormal(c) => c.color
    }

    if leadOpt.isEmpty then true
    else
      val lead = leadOpt.get
      val mustFollow =
        player.hand.exists(c => isNormal(c) && c.color == lead)

      if mustFollow then isNormal(card) && card.color == lead else true


  private[control] def doScoreRound(gs: GameState): GameState =
    val scored = gs.players.map { p =>
      p.copy(totalPoints = p.totalPoints + calculateRoundPoints(p))
    }

    val s1 = gs.copy(players = scored)
    s1.notifyObservers()
    view.showRoundEvaluation(s1.currentRound, scored)

    val reset = scored.map(p => p.copy(tricks = 0, predictedTricks = 0))
    val s2 = s1.copy(players = reset)
    s2.notifyObservers()
    s2


  private def calculateRoundPoints(p: Player): Int =
    if p.predictedTricks == p.tricks then
      20 + p.tricks * 10
    else
      p.tricks * 10 - 10 * (p.tricks - p.predictedTricks).abs


  private[control] def doDetermineWinner(gs: GameState): Unit =
    val winner = gs.players.maxBy(_.totalPoints)
    view.showGameWinner(winner)
}
