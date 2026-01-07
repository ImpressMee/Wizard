package de.htwg.wizard.view

import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.observer.Observer
import de.htwg.wizard.model.*

class TuiView extends Observer {

  private def colorCard(c: Card): String =
    c.cardType match
      case CardType.Wizard => s"${Console.RED}${c}${Console.RESET}"
      case CardType.Joker  => s"${Console.YELLOW}${c}${Console.RESET}"
      case CardType.Normal(_) =>
        val col = c.color match
          case CardColor.Red    => Console.RED
          case CardColor.Blue   => Console.BLUE
          case CardColor.Green  => Console.GREEN
          case CardColor.Yellow => Console.YELLOW
        s"$col$c${Console.RESET}"

  override def update(event: GameEvent): Unit =
    event match

      // ===============================
      // Spielstart
      // ===============================
      case PlayerAmountRequested(_) =>
        println(s"${Console.MAGENTA}[TUI] Waiting for player count from GUI...${Console.RESET}")

      // ===============================
      // Runde gestartet
      // ===============================
      case RoundStarted(round, state) =>
        println(s"\n${Console.BLUE}========== ROUND $round ==========${Console.RESET}")
        println(s"Trumpf: ${state.currentTrump.getOrElse("None")}")

      // ===============================
      // Vorhersagen
      // ===============================
      case PredictionsRequested(state) =>
        println(s"\n${Console.MAGENTA}[TUI] Prediction phase${Console.RESET}")
        state.players.foreach { p =>
          val cards = p.hand.map(colorCard).mkString(", ")
          println(s"${Console.CYAN}Player ${p.id}${Console.RESET}: $cards")
        }

      // ===============================
      // Stichstart
      // ===============================
      case TrickMoveRequested(trickNr, _) =>
        println(s"\n${Console.BLUE}[TUI] Trick $trickNr started${Console.RESET}")

      // ===============================
      // Stich beendet
      // ===============================
      case TrickFinished(winnerId, _) =>
        println(
          s"${Console.GREEN}[TUI] Trick won by Player $winnerId${Console.RESET}"
        )

      // ===============================
      // Runde beendet
      // ===============================
      case RoundFinished(state) =>
        println(s"\n${Console.MAGENTA}========== ROUND FINISHED ==========${Console.RESET}")
        state.players.foreach { p =>
          println(
            s"${Console.CYAN}Player ${p.id}${Console.RESET}: " +
              s"predicted=${p.predictedTricks}, tricks=${p.tricks}, points=${p.totalPoints}"
          )
        }

      // ===============================
      // Spiel beendet
      // ===============================
      case GameFinished(winner, _) =>
        println(
          s"\n${Console.RED}GAME FINISHED — Winner: Player ${winner.id} (${winner.totalPoints} points)${Console.RESET}"
        )

      case _ => ()
}
