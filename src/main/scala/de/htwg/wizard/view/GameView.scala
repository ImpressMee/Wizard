package de.htwg.wizard.view

import de.htwg.wizard.control.Observer
import de.htwg.wizard.model.*
import scala.io.StdIn.readLine
/**
 * The order of the methods reflect the order of the gameflow
 */

class GameView extends Observer{
  def writeOneCard(card: Card): String =
    card.cardType match
      case CardType.Wizard => s"${card.color} WIZARD"
      case CardType.Joker => s"${card.color} JOKER"
      case CardType.Normal(v) => s"${card.color} $v"


  def askPlayerAmount(): Unit =
    println(s"${Console.RED}/////////----Game Start----/////////")
    print(s"How many Players are playing? (3-6): ${Console.RESET}")

  def chooseTrump(): CardColor =
    println("Trumpfkarte ist ein WIZARD – Du darfst die Trumpffarbe bestimmen:")
    println("Wähle die Farbe:")
    println("1 = Red, 2 = Green, 3 = Blue, 4 = Yellow")

    readLine().trim match
      case "1" => CardColor.Red
      case "2" => CardColor.Green
      case "3" => CardColor.Blue
      case "4" => CardColor.Yellow
      case _ =>
        println("Ungültige Eingabe – bitte erneut wählen.")
        chooseTrump()


  def readPlayerAmount(): Int =
    val input = readLine()
    try
      val playerCount = input.toInt
      if playerCount < 3 || playerCount > 6 then
        showError("Wrong amount! Try again.")
        readPlayerAmount()
      else
        playerCount

    catch
      case _: NumberFormatException =>
        println("Please enter a valid number!")
        readPlayerAmount()

  def showRoundInfo(round: Int, trump: Option[CardColor], numberOfPlayers: Int): Unit =
    val trumpText = trump match
      case Some(color) => s"Trump color is: $color"
      case None => "there is no trump"

    println(
      s"""\n\n${Console.MAGENTA}////////////////////////////////////////////////////////////
         |/////----Round $round start----//////
         |----Rundeninfo-------------------
         |There are $numberOfPlayers players.
         |round: $round
         |$trumpText
         |---------------------------------${Console.RESET}
         |""".stripMargin
    )


  def colorize(card: Card): String =
    val color = card.color match
      case CardColor.Red => Console.RED
      case CardColor.Blue => Console.BLUE
      case CardColor.Green => Console.GREEN
      case CardColor.Yellow => Console.YELLOW

    s"$color${writeOneCard(card)}${Console.RESET}"

  def showPlayerCards(player :Player): Unit =
    val coloredCards = player.hand.map(colorize).mkString(", ")
      println(
        s"""\n\n-----------------------------------------------
           |${Console.CYAN}| Player${player.id}|${Console.RESET}
           |+---------------------------------+
           || Cards: $coloredCards
           |+---------------------------------+
           |-----------------------------------------------
           |""".stripMargin
      )

  def askHowManyTricks(player: Player): Unit =
    showPlayerCards(player)
    print(s"\nHow many tricks will you make player${player.id}?\n")


  def readPositiveInt(): Int =
    val input = readLine()
    try
      val value = input.toInt
      if value >= 0 then
        value
      else
        println("Index out of range! Try again.")
        readPositiveInt()
    catch
      case _: NumberFormatException =>
        println("Please enter a valid number!")
        readPositiveInt()



  def showTrickStart(trickNr: Int): Unit =
    println(s"${Console.BLUE}//////////////////////////////")
    println(s"\n\n///----Trick ${trickNr} start----///\n${Console.RESET}")

  def askPlayerCard(player: Player): Unit =
    showPlayerCards(player)
    println(s"Which card do you wanna play Player${player.id}? (Index starts by 1)")

  def readIndex(player: Player): Int =
    val input = readLine()
    try
      val index = input.toInt-1
      if index >= 0 && index < player.hand.length then
        index
      else
        println("Index out of range! Try again.")
        readIndex(player)
    catch
      case _: NumberFormatException =>
        println("Please enter a valid number!")
        readIndex(player)

  def showTrickWinner(player: Player, winningCard: Card): Unit =
    println(s"${Console.BLUE}///----Trick Winner----///")
    println(s"\n--Player${player.id} won this trick with ${writeOneCard(winningCard)}")
    println(s"//////////////////////////////${Console.RESET}")

  def showRoundEvaluation(round: Int, players: List[Player]): Unit =
    println(s"\n${Console.MAGENTA}//////--Round $round -- Evaluation --//////")
    for p <- players do
      println(
        s"""
           |${Console.CYAN}------ Player ${p.id} -------
           |tricks predicted: ${p.predictedTricks}
           |actual tricks:    ${p.tricks}
           |=> Player ${p.id} has ${p.totalPoints} points in total in Round $round
           |////////////////////////////////////////////////////////////${Console.RESET}
           |""".stripMargin
      )

  def showGameWinner(player: Player): Unit =
    println(s"${Console.RED}/////////----Game Winner----/////////")
    println(s"\nAnd the winner is Player${player.id} with ${player.totalPoints} points${Console.RESET}")

  def showError(message: String): Unit =
    println(Console.RED + message + Console.RESET)

  override def update(): Unit=
    println("update display")   // update display



}

