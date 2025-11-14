package de.htwg.wizard.view

import de.htwg.wizard.model.{Card, CardColor, Player}
import de.htwg.wizard.util.Observer

/**
 * The order of the methods reflect the order of the gameflow
 */

class GameView extends Observer{
  def askPlayerAmount(): Unit =
    print("How many Players are playing? (3-6): ")

  def showRoundInfo(round: Int, trump: CardColor, numberOfPlayers: Int): Unit =
    println(
      s"""//////////////////////////////////////////
         |There are $numberOfPlayers players. \n
         |round: $round \n
         |Trump is: $trump\n\n
         |""".stripMargin
    )

  def showPlayerCards(players: List[Player]): Unit =
    for p <- players do
      println(
        s"""-----------------------------------------------
           |+-----------+
           |${Console.GREEN}| Player ${p.id}|${Console.RESET}
           |+-----------+
           || Cards: ${p.hand.mkString(", ")}
           |+-----------+
           |""".stripMargin
      )

  def askNewStitches(player: Player): Unit =
    println("==========================================")
    print(s"How many Stitches will you make player${player.id}?\n")

  def showStitchStart(): Unit =
    println("\n\n//////----Stitch start----//////\n")

  def askPlayerCard(player: Player): Unit =
    println(
      s"""_____________________________________________
         |+-----------+
         |${Console.GREEN}| Player ${player.id}|${Console.RESET}
         |+-----------+
         || Cards: ${player.hand.mkString(", ")}
         |+-----------+
         |""".stripMargin.trim
    )
    println(s"Which card do you wanna play Player ${player.id}? (Index starts by 0)")

  def showStitchWinner(player: Player, winningCard: Card): Unit =
    println(s"\n--Player ${player.id} won this stitch with $winningCard")

  def showRoundEvaluation(round: Int, players: List[Player]): Unit =
    println(s"\n--Round $round -- Evaluation --")
    for p <- players do
      println(
        s"""
           |------ Player ${p.id} -------
           |stitches predicted: ${p.predictedStitches}
           |actual stitches:    ${p.stitches}
           |=> Player ${p.id} has ${p.totalPoints} points in Round $round
           |""".stripMargin
      )

  def showGameWinner(player: Player): Unit =
    println(s"\nAnd the winner is Player${player.id} mit ${player.totalPoints}")

  def showError(msg: String): Unit =
    println(Console.RED + msg + Console.RESET)

  override def update(): Unit =
    print("Do sumthin, idk?")
}

