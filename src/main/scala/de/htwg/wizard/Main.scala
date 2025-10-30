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
  val rounds = 1 // aktuell magic number, wird noch dynamisch angepasst an Spieleranzahl
  val number_of_players = getPlayerCount()

  println(stringBeginningRound(number_of_players, rounds))
  stringPlayerAndCards(number_of_players, rounds).foreach(println)
/**
 * @param input
 * @return
 * nimmt einen String an der beim aufrufen eingelesen wird.
 * Hinzugefügt um das testen möglich zu machen.
 */
def getPlayerCount(input: => String = readLine()): Int =
  print("Wie viele Spieler machen mit? (3-6): ")
  try
    val player_count = input.toInt
    if player_count >= 3 && player_count <= 6 then {
      player_count // <- Rückgabewert
    } else
      println(Console.RED + "\nFalsche Anzahl! Bitte erneut versuchen.\n" + Console.RESET)
      getPlayerCount(input) // <- Rekursion, es wird erneut versucht
  catch
    case _: NumberFormatException =>
      println(Console.RED + "\nUngültige Eingabe! Bitte eine Zahl eingeben.\n" + Console.RESET)
      getPlayerCount(input) // <- Rekursion, es wird erneut versucht

/**
 * @param number_of_players
 * @param rounds
 */
def stringBeginningRound(number_of_players: Int, rounds: Int): String =
  val round = rounds
  val trump = "Card x,y"
  val returnString =
    s"""There are $number_of_players players. \n
       |round: $round \n
       |Trump is: $trump\n\n
       |    """.stripMargin
  returnString

/**
 * println(s"There are $number_of_players players.\n")
 *
 * // Start der Runde
 * println(s"round: $round")
 * println(s"Trump is: $trump\n")
 */
def stringPlayerAndCards(number_of_players: Int, rounds: Int): Array[String] =
  val playerStrings = new Array[String](number_of_players)
  for (i <- 1 to number_of_players) {
    val eachPlayerString =
      s"""
         |+-----------+
         |${Console.GREEN}| Player $i|${Console.RESET}
         |+-----------+
         || Cards: -
         |+-----------+
         |""".stripMargin
    playerStrings(i - 1) = eachPlayerString
  }
  playerStrings
