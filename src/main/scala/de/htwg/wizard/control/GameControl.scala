package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import de.htwg.wizard.control.strategy.{StandardTrickStrategy, TrickStrategy}

import scala.util.{Try, Success, Failure}
import scala.util.boundary
import scala.util.boundary.break

class GameControl(
                   val view: GameView,
                   val strategy: TrickStrategy = StandardTrickStrategy()
                 ) {

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

  // ============================================================
  // INITIALIZATION
  // ============================================================

  private[control] def doInitGame(): GameState =
    view.askPlayerAmount()

    view.readPlayerAmount() match
      case Success(playerCount) =>
        if playerCount < 3 || playerCount > 6 then
          view.showError("Wrong amount! Try again.")
          doInitGame()
        else
          val deck = new Deck().shuffle()
          val players = (0 until playerCount).map(id => Player(id)).toList
  
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


  // ============================================================
  // PREPARE NEXT ROUND
  // ============================================================

  private[control] def doPrepareNextRound(gs: GameState): GameState = {
      saveState(gs)
      if gs.currentRound >= gs.totalRounds then
        gs
      else
        val newRound = gs.currentRound + 1

        // IMPORTANT: create a fresh full deck every round
        val fullDeck = Deck().shuffle()

        val trumpCard = fullDeck.cards.head
        val deckAfterTrump = fullDeck.copy(cards = fullDeck.cards.tail)

        val trump =
          if isWizard(trumpCard) then
            Some(view.chooseTrump())
          else if isJoker(trumpCard) then
            None
          else
            Some(trumpCard.color)

        view.showRoundInfo(newRound, trump, gs.amountOfPlayers)

        var remainingDeck = deckAfterTrump
        val playersWithHands =
          gs.players.map { player =>
            val (hand, newDeck) = remainingDeck.deal(newRound)
            remainingDeck = newDeck
            player.copy(hand = hand)
          }

        gs.copy(
          currentRound = newRound,
          players = playersWithHands,
          deck = remainingDeck,
          currentTrump = trump
        )
  }


  // ============================================================
  // PREDICT TRICKS
  // ============================================================

  private[control] def doPredictTricks(gs: GameState): GameState =
    val players = gs.players.map { p =>
      view.askHowManyTricks(p)
      val prediction = view.readPositiveInt()
      p.copy(predictedTricks = prediction)
    }
    gs.copy(players = players)

  // ============================================================
  // PLAY ONE TRICK
  // ============================================================

  private[control] def doPlayOneTrick(n: Int, gs: GameState): GameState =
    internalPlayOneTrick(n, gs)

  private def internalPlayOneTrick(trickNumber: Int, gameState: GameState): GameState =
    boundary:

      // Abort if at least one player has no cards left
      // In that case, no valid trick can be played
      if gameState.players.exists(_.hand.isEmpty) then
        view.showError("No active stitch!")
        break(gameState)

      // Inform the view that a new trick starts
      view.showTrickStart(trickNumber)

      // Collects the cards played so far (playerId -> card)
      // Used to enforce rules like "follow suit"
      var partialTrick = Trick(Map.empty)

      // ------------------------------------------------------------
      // Phase 1: Collect all intended moves WITHOUT modifying hands
      // ------------------------------------------------------------
      val collectedMovesOpt = boundary: //boundary creates a controlled early-exit context.
        val collectedMoves = gameState.players.map { player =>
          // Ask the player to choose a card
          view.askPlayerCard(player)
          val cardIndex = view.readIndex(player)

          // Validate index
          if cardIndex < 0 || cardIndex >= player.hand.size then
            view.showError("Invalid index!")
            break(None) // can use break(value) to exit immediately
                        // and return a value from the boundary expression.

          val chosenCard = player.hand(cardIndex)

          // Validate game rules (e.g. follow suit)
          if !isAllowedMove(chosenCard, player, partialTrick) then
            view.showError("You must follow the start color!")
            break(None)

          // Temporarily record the card as played
          partialTrick =
            Trick(partialTrick.played + (player.id -> chosenCard))

          // Store move information:
          // (playerId, index in hand, chosen card)
          (player.id, cardIndex, chosenCard)
        }

        Some(collectedMoves)

      // ------------------------------------------------------------
      // If any move was invalid, abort the trick completely
      // IMPORTANT: No card has been removed so far
      // ------------------------------------------------------------
      if collectedMovesOpt.isEmpty then
        return gameState

      val collectedMoves = collectedMovesOpt.get

      // ------------------------------------------------------------
      // Phase 2: Apply moves – now cards are actually removed
      // ------------------------------------------------------------
      val playersAfterCardRemoval =
        gameState.players.map { player =>
          collectedMoves.find(_._1 == player.id) match
            case Some((_, cardIndex, _)) =>
              player.copy(hand = player.hand.patch(cardIndex, Nil, 1))
            case None =>
              player
        }

      // Build the final Trick object from the collected moves
      val completedTrick =
        Trick(collectedMoves.map { (id, _, card) => id -> card }.toMap)

      // Update game state with played trick
      val stateAfterTrick =
        gameState.copy(
          players = playersAfterCardRemoval,
          currentTrick = Some(completedTrick)
        )

      // Notify observers (GUI/TUI)
      stateAfterTrick.notifyObservers()

      // ------------------------------------------------------------
      // Determine trick winner using the configured strategy
      // ------------------------------------------------------------
      val (winningPlayerId, winningCard) =
        strategy.winner(completedTrick, stateAfterTrick.currentTrump)

      // Increment trick count for the winner
      val playersAfterScoring =
        stateAfterTrick.players.map { player =>
          if player.id == winningPlayerId then
            player.copy(tricks = player.tricks + 1)
          else
            player
        }

      val winningPlayer =
        playersAfterScoring.find(_.id == winningPlayerId).get

      // Show winner in the view
      view.showTrickWinner(winningPlayer, winningCard)

      // Return updated state, trick is finished
      stateAfterTrick.copy(
        players = playersAfterScoring,
        currentTrick = None
      )


  // ============================================================
  // RULES
  // ============================================================

  private def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
    val leadOpt = trick.played.values.collectFirst {
      case c if isNormal(c) => c.color
    }

    if leadOpt.isEmpty then true
    else
      val lead = leadOpt.get
      val mustFollow =
        player.hand.exists(c => isNormal(c) && c.color == lead)

      if mustFollow then isNormal(card) && card.color == lead
      else true

  // ============================================================
  // SCORE ROUND
  // ============================================================

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

  // ============================================================
  // DETERMINE WINNER
  // ============================================================

  private[control] def doDetermineWinner(gs: GameState): Unit =
    val winner = gs.players.maxBy(_.totalPoints)
    view.showGameWinner(winner)
}
