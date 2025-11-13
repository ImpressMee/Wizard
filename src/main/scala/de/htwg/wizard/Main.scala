package de.htwg.wizard

import scala.collection.mutable.Stack
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
  game()

/**
 * @param input
 * @return
 * nimmt einen String an der beim aufrufen eingelesen wird.
 * Hinzugefügt um das testen möglich zu machen.
 */
def getPlayerCount(input: => String = readLine()): Int =
  print("How many Players are playing? (3-6): ")
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
def stringBeginningRound(number_of_players: Int, rounds: Int): (String, String) =
  val colors = Array("red", "green", "blue", "yellow")
  val randomColor = colors(scala.util.Random.nextInt(colors.length))
  val round = rounds
  val trump = s"${randomColor}"
  val returnString =
    s"""//////////////////////////////////////////
       |There are $number_of_players players. \n
       |round: $round \n
       |Trump is: $trump\n\n
       |""".stripMargin
  (returnString, trump)

def shuffle(cardarray: Array[String]): Stack[String] = {
  val mixed = Stack.from(scala.util.Random.shuffle(cardarray.toList))
  return mixed
}

def dealcards(number_of_players: Int, rounds: Int, cardStack: Stack[String]): Array[Array[String]] = {
  val playerarrays = Array.ofDim[String](number_of_players, rounds)
  for (r <- 0 until rounds) {
    for (np <- 0 until number_of_players) {
      playerarrays(np)(r) = cardStack.pop()
    }
  }
  playerarrays
}
/**
 * println(s"There are $number_of_players players.\n")
 *
 * // Start der Runde
 * println(s"round: $round")
 * println(s"Trump is: $trump\n")
 */
def round(number_of_players: Int, rounds: Int, cardarray: Array[String]): Array[Int] =
  val shuffeledCardStack = shuffle(cardarray)
  val playercards = dealcards(number_of_players, rounds, shuffeledCardStack)
  val (beginString, trump) = stringBeginningRound(number_of_players, rounds)
  println(beginString)

  val playerStrings = new Array[String](number_of_players)
  for (i <- 0 to number_of_players - 1) {
    val eachPlayerString =
      s"""-----------------------------------------------
         |+-----------+
         |${Console.GREEN}| Player $i|${Console.RESET}
         |+-----------+
         || Cards: ${playercards(i).mkString(", ")}
         |+-----------+
         |""".stripMargin.trim
    playerStrings(i) = eachPlayerString
    println(eachPlayerString)
  }

  val stitchprediction = stitchPrediction(number_of_players = number_of_players)
  val stitches = stitchgame(number_of_players, rounds, playercards, trump)
  val points = stitchPointsCalculator(stitchprediction, stitches, number_of_players)

  println(s"\n--Round $rounds -- Evaluation --")
  for (i <- 0 until number_of_players) {
    println(s"\n------ Player${i} -------")
    println(s"stitches predicted ${stitchprediction(i)}")
    println(s"actual stitches ${stitches(i)}")
    println(s"=> Player${i} has ${points(i)} Points in Round ${rounds}")
  }

  points

def stitchPointsCalculator(stitchprediction: Array[Int], stitches: Array[Int], number_of_players: Int): Array[Int] = {
  val stitchpoints = Array.ofDim[Int](number_of_players)

  for (i <- 0 until number_of_players) {
    if (stitchprediction(i) == stitches(i)) {
      stitchpoints(i) = 20 + stitches(i) * 10
    } else {
      stitchpoints(i) = -10 * (stitches(i) - stitchprediction(i)).abs
    }
  }
  stitchpoints
}

def stitchPrediction(input: => String = readLine(), number_of_players: Int): Array[Int] = {
  val playerguess = Array.ofDim[Int](number_of_players)
  for (i <- 0 to number_of_players - 1) {
    println("==========================================")
    print(s"How many Stitches will you make player${i}?\n")
    try
      val stitchguess = input.toInt
      playerguess(i) = stitchguess
    catch
      case _: NumberFormatException =>
        println(Console.RED + "\nUngültige Eingabe! Bitte eine Zahl eingeben.\n" + Console.RESET)
  }
  playerguess
}

def stitchgame(
                number_of_players: Int,
                rounds: Int,
                playercards: Array[Array[String]],
                trump: String,
                input: => String = readLine()
              ): Array[Int] = {
  var currentCards = playercards
  val stitches = Array.ofDim[Int](number_of_players)
  for (_ <- 0 until rounds) {
    println("\n\n//////----New Stitch----//////\n")
    val (playedCards, newPlayerCards) = oneStitch(number_of_players, rounds, currentCards, trump)
    currentCards= newPlayerCards
    val (winner, winningCard) = whoWonStitch(playedCards, number_of_players, trump)
    stitches(winner) = stitches(winner) + 1
    println(s"\n --Player$winner wonn this Stitch With $winningCard")
  }
  stitches
}

def oneStitch(
               number_of_players: Int,
               rounds: Int,
               playercards: Array[Array[String]],
               trump: String,
               input: => String = readLine()
             ): (Array[String], Array[Array[String]]) ={
  val playedCards = Array.ofDim[String](number_of_players)

  for (i <- 0 to number_of_players - 1) {
    val eachPlayerString =
      s"""_____________________________________________
         |+-----------+
         |${Console.GREEN}| Player $i|${Console.RESET}
         |+-----------+
         || Cards: ${playercards(i).mkString(", ")}
         |+-----------+
         |""".stripMargin.trim
    println(eachPlayerString)
    println(s"Which card do you wanna play Player${i}? (Index starts by 0)")
    var valid = false
    while (!valid) {
      try
        val index = input.toInt
        if (index >= 0 && index < playercards(i).length) {
          playedCards(i) = playercards(i)(index)
          // remove Card
          playercards(i) = playercards(i).patch(index, Nil, 1)

          valid = true
        } else {
          println(Console.RED + "Index out of range!" + Console.RESET)
        }
      catch
        case _: NumberFormatException =>
          println(Console.RED + "\nUngültige Eingabe! Bitte eine Zahl eingeben.\n" + Console.RESET)
    }
    println(s"Player${i} played ${playedCards(i)}")
  }
  (playedCards, playercards)
}
def whoWonStitch(playedCards: Array[String], number_of_players:Int, trump: String): (Int, String) = {
  val colorArr = Array.ofDim[String](number_of_players)
  val numArr = Array.ofDim[Int](number_of_players)
  //split Card Strings
  for (i <- 0 until number_of_players) {
    val Array(color, numStr) = playedCards(i).split(" ")
    val number = numStr.toInt
    numArr(i)=number
    colorArr(i)=color
  }
  //find best card
  var best = -1
  for (i <- 0 until number_of_players) {
    if(colorArr(i)==trump) {
      if(best == -1 || colorArr(best)!=trump){
        best =i
      }else if(numArr(i)>numArr(best)){
          best = i
      }
    } else{
      if (best == -1) {
        best = i
      } else if(colorArr(best)!=trump && numArr(i)>numArr(best)){
        best = i
      }
    }
  }

  val winner = best
  val winningCard = playedCards(best)

  (best , winningCard)
}

def game(): Unit = {

  val cardarray = Array(
    "blue 1", "blue 2", "blue 3",
    "green 1", "green 2", "green 3",
    "red 1", "red 2", "red 3",
    "yellow 1", "yellow 2", "yellow 3"
  )
  val playercount = getPlayerCount()
  val roundvalue = Array(4, 3, 2, 2)
  val roundcount = roundvalue(playercount - 3)
  val totalPoints = Array.fill(playercount)(0)


  var best = 0
  for (i <- 1 to roundcount) {
    val roundPoints = round(playercount, i, cardarray)
    for (j <- 0 until playercount) {
      totalPoints(j) += roundPoints(j)

    }
  }
  println("\n\n ----Endpoints and Winner ----")
  for (i <- 0 until playercount) {
    println(s"Player${i} has ${totalPoints(i)}")
    if (totalPoints(i) > totalPoints(best)) {
      best = i
    }
  }
  println(s"\nAnd the winner is Player${best} mit ${totalPoints(best)}")
}