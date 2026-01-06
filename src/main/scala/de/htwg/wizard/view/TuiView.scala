package de.htwg.wizard.view

import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.observer.Observer
import de.htwg.wizard.model.*

class TuiView extends Observer {

  // ============================================================
  // Observer entry point
  // ============================================================

  override def update(event: GameEvent): Unit =
    event match

      case RoundStarted(round, state) =>
        showRoundInfo(round, state.currentTrump, state.amountOfPlayers)
        render(state)

      case TrickStarted(n, state) =>
        showTrickStart(n)
        render(state)

      case TrickFinished(winnerId, state) =>
        val winner = state.players.find(_.id == winnerId).get
        val winningCard =
          state.currentTrick.flatMap(_.played.get(winnerId))

        winningCard.foreach(card =>
          showTrickWinner(winner, card)
        )

      case RoundFinished(state) =>
        showRoundEvaluation(state.currentRound, state.players)
        render(state)

      case GameFinished(winner, state) =>
        render(state)
        showGameWinner(winner)

      case StateChanged(state) =>
        render(state)

  // ============================================================
  // Rendering (pure output, no logic)
  // ============================================================

  private def render(state: GameState): Unit =
    println("\n========== GAME STATE ==========")
    println(s"Round: ${state.currentRound}")
    println(s"Trump: ${state.currentTrump.getOrElse("None")}")

    state.players.foreach { p =>
      showPlayerCards(p)
      println(
        s"Predicted: ${p.predictedTricks}, " +
          s"Tricks: ${p.tricks}, " +
          s"Points: ${p.totalPoints}"
      )
    }

    state.currentTrick.foreach { trick =>
      println("\nCurrent Trick:")
      trick.played.foreach { (pid, card) =>
        println(s"Player $pid played ${writeOneCard(card)}")
      }
    }

  // ============================================================
  // Existing display helpers (unchanged)
  // ============================================================

  def writeOneCard(card: Card): String =
    card.cardType match
      case CardType.Wizard      => s"${card.color} WIZARD"
      case CardType.Joker       => s"${card.color} JOKER"
      case CardType.Normal(v)   => s"${card.color} $v"

  private def colorize(card: Card): String =
    val color = card.color match
      case CardColor.Red    => Console.RED
      case CardColor.Blue   => Console.BLUE
      case CardColor.Green  => Console.GREEN
      case CardColor.Yellow => Console.YELLOW

    s"$color${writeOneCard(card)}${Console.RESET}"

  def showPlayerCards(player: Player): Unit =
    val cards = player.hand.map(colorize).mkString(", ")
    println(
      s"""
         |-----------------------------------------------
         |${Console.CYAN}| Player ${player.id} |${Console.RESET}
         |Cards: $cards
         |-----------------------------------------------
         |""".stripMargin
    )

  def showRoundInfo(round: Int, trump: Option[CardColor], numberOfPlayers: Int): Unit =
    val trumpText = trump.map(_.toString).getOrElse("None")
    println(
      s"""
         |========== ROUND $round ==========
         |Players: $numberOfPlayers
         |Trump: $trumpText
         |==================================
         |""".stripMargin
    )

  def showTrickStart(n: Int): Unit =
    println(s"\n--- Trick $n start ---")

  def showTrickWinner(player: Player, card: Card): Unit =
    println(s"\nPlayer ${player.id} won the trick with ${writeOneCard(card)}")

  def showRoundEvaluation(round: Int, players: List[Player]): Unit =
    println(s"\n=== Round $round Evaluation ===")
    players.foreach { p =>
      println(
        s"Player ${p.id}: " +
          s"predicted=${p.predictedTricks}, " +
          s"tricks=${p.tricks}, " +
          s"points=${p.totalPoints}"
      )
    }

  def showGameWinner(player: Player): Unit =
    println(
      s"""
         |========== GAME OVER ==========
         |Winner: Player ${player.id}
         |Points: ${player.totalPoints}
         |================================
         |""".stripMargin
    )
}
