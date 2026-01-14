package de.htwg.wizard.view

import de.htwg.wizard.control.{GameEvent, GameFinished, Observer, PlayerAmountRequested, PredictionsRequested, RoundFinished, RoundStarted, TrickFinished, TrickMoveRequested}
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Player}

class TuiView extends Observer {

  // =========================================================
  // Coloring (only via card.color)
  // =========================================================

  private def colorCard(card: Card): String = {
    val color = card.color match
      case CardColor.Red    => Console.RED
      case CardColor.Blue   => Console.BLUE
      case CardColor.Green  => Console.GREEN
      case CardColor.Yellow => Console.YELLOW

    s"$color${card.toString}${Console.RESET}"
  }

  private def showPlayerCards(player: Player): Unit = {
    val cards = player.hand.map(colorCard).mkString(", ")
    println(
      s"""
         |-----------------------------------------------
         |${Console.CYAN}| Player ${player.id} |${Console.RESET}
         |Cards: $cards
         |-----------------------------------------------
         |""".stripMargin
    )
  }

  // =========================================================
  // Observer
  // =========================================================

  override def update(event: GameEvent): Unit =
    event match

      // ===============================
      // Game start
      // ===============================
      case PlayerAmountRequested(_) =>
        println(
          s"""
             |${Console.RED}/////////----Game Start----/////////${Console.RESET}
             |How many players are playing? (3–6)
             |""".stripMargin
        )

      // ===============================
      // Round started
      // ===============================
      case RoundStarted(round, state) =>
        val trumpText = state.currentTrump match
          case Some(c) => s"Trump color is: $c"
          case None    => "There is no trump"

        println(
          s"""
             |${Console.MAGENTA}////////////////////////////////////////////////////////////
             |/////----Round $round start----//////
             |----Round info----------------------
             |There are ${state.players.size} players.
             |Round: $round
             |$trumpText
             |------------------------------------${Console.RESET}
             |""".stripMargin
        )

      // ===============================
      // Prediction phase
      // ===============================
      case PredictionsRequested(state) =>
        println(s"\n${Console.MAGENTA}//// Prediction Phase ////${Console.RESET}")
        state.players.foreach { p =>
          showPlayerCards(p)
          println(s"How many tricks will you make Player ${p.id}?")
        }

      // ===============================
      // Trick started
      // ===============================
      case TrickMoveRequested(trickNr, state) =>
        println(
          s"""
             |${Console.BLUE}//////////////////////////////
             |///----Trick $trickNr start----///
             |//////////////////////////////${Console.RESET}
             |""".stripMargin
        )
        state.players.foreach { p =>
          showPlayerCards(p)
          println(s"Which card do you want to play Player ${p.id}? (index)")
        }

      // ===============================
      // Trick finished
      // ===============================
      case TrickFinished(winnerId, _) =>
        println(
          s"${Console.GREEN}///----Trick won by Player $winnerId----///${Console.RESET}"
        )

      // ===============================
      // Round finished
      // ===============================
      case RoundFinished(state) =>
        println(
          s"\n${Console.MAGENTA}//////--Round Evaluation--//////${Console.RESET}"
        )
        state.players.foreach { p =>
          println(
            s"""
               |${Console.CYAN}------ Player ${p.id} -------
               |tricks predicted: ${p.predictedTricks}
               |actual tricks:    ${p.tricks}
               |=> total points:  ${p.totalPoints}
               |------------------------------------${Console.RESET}
               |""".stripMargin
          )
        }
        println("Press ENTER / click 'Weiter' in GUI to continue...")

      // ===============================
      // Game finished
      // ===============================
      case GameFinished(winner, _) =>
        println(
          s"""
             |${Console.RED}/////////----Game Winner----/////////
             |Winner: Player ${winner.id}
             |Total points: ${winner.totalPoints}
             |////////////////////////////////////${Console.RESET}
             |""".stripMargin
        )

      case _ => ()
}
