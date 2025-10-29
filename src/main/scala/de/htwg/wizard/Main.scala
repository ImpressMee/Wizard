package de.htwg.wizard

import scala.io.StdIn.*

/**
 * @author Justin-Jay Balaba
 * @author Nikita Kusch
 *         TODO: Es sollte noch alles ins englische übersetzt werden
 *         TODO: var -> val machen.
 *         TODO: Speichere Prediction in eine Map mit dem Spieler
 *         TODO: Spielpunkte werden berechnen
 *         TODO: Runden anzahl auf Spieleranzahl anpassen (3s = 20r; 4s = 15r; 5s = 12r; 6s = 10r)
 *
 */

@main def main(): Unit =
  val number_of_players = getPlayerCount
  val tui: Unit = print_tui(number_of_players, 1)
  val stitch_pred: Unit = stitch_prediction(number_of_players, 1)


def getPlayerCount: Int =
  print("Wie viele Spieler machen mit? (3-6): ")
  try
    val player_count = readLine().toInt
    if player_count >= 3 && player_count <= 6 then {
      player_count // <- Rückgabewert
    } else
      println(Console.RED + "\nFalsche Anzahl! Bitte erneut versuchen.\n" + Console.RESET)
      getPlayerCount // <- Rekursion, es wird erneut versucht
  catch
    case _: NumberFormatException =>
      println(Console.RED + "\nUngültige Eingabe! Bitte eine Zahl eingeben.\n" + Console.RESET)
      getPlayerCount // <- Rekursion, es wird erneut versucht

def print_tui(number_of_players: Int, rounds: Int): Unit =
  val round = rounds
  val trump = "Card x,y"
  println(s"There are $number_of_players players.\n")

  // Start der Runde
  println(s"round: $round")
  println(s"Trump is: $trump\n")

  for i <- 1 to number_of_players do
    println("\n+-----------+")
    println(Console.GREEN + s"| Player $i |" + Console.RESET)
    println("+-----------+")
    println("| Cards: -")
    println("+-----------+")

def stitch_prediction(number_of_players: Int, round: Int): Unit =
  // win predictions
  def askPlayer(player: Int): Int =
    println("+-----------")
    println(s"\nPlayer $player, what is your prediction?")
    try
      val prediction = readLine().toInt
      if prediction <= round then {
        println(s"\nPlayer $player predicts $prediction wins")
        prediction // <- Rückgabe wert
      } else
        println(Console.RED + "\nInvalid number! Please enter a number.\n" +
          "! The prediction should not be bigger than the current round !.\n" + Console.RESET)
        askPlayer(player) // <- recursion in case of failure
    catch
      case _: NumberFormatException =>
        println(Console.RED + "\nPlease enter a number fitting for the current round!\n" + Console.RESET);
        askPlayer(player) // <- recursion in case of failure

  for player <- 1 to number_of_players do
    val prediction = askPlayer(player)


