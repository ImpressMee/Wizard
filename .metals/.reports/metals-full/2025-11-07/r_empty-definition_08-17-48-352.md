error id: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/main/scala/de/htwg/wizard/Main.scala:`<none>`.
file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/main/scala/de/htwg/wizard/Main.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3637
uri: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/main/scala/de/htwg/wizard/Main.scala
text:
```scala
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
def stringBeginningRound(number_of_players: Int, rounds: Int): String =
  val round = rounds
  val trump = "Card x,y"
  val returnString =
    s"""-----------------------------------------------------
There are $number_of_players players. \n
round: $round \n
Trump is: $trump\n\n
    """.stripMargin
  returnString

def shuffle(cardarray: Array[String]): Stack[String] = {
  val mixed =Stack.from(scala.util.Random.shuffle(cardarray.toList))
  return mixed
}
def dealcards(number_of_players:Int, rounds:Int, cardStack: Stack[String]): Array[Array[String]] = {
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
def round(number_of_players: Int, rounds: Int, cardarray:Array[String]): Array[String] =
  val shuffeledCardStack=shuffle(cardarray)
  val playercards = dealcards(number_of_players, rounds, shuffeledCardStack)
  println(stringBeginningRound(number_of_players, rounds))

  val playerStrings = new Array[String](number_of_players)
  for (i <- 0 to number_of_players-1) {
    val eachPlayerString =
      s"""
+-----------+
${Console.GREEN}| Player $i|${Console.RESET}
+-----------+
 Cards: ${playercards(i).mkString(", ")}
+-----------+
""".stripMargin.trim
    playerStrings(i) = eachPlayerString
  }
  val stitchprediction = stitchPrediction(number_of_players = number_of_players)
  val stitches = stitchgame(number_of_players, rounds, playercards)
  stitchPointsCalculator(stichprediction, stitches, number_of_players)
  println(s"stitches: ${stitches.mkString(", ")}")
  playerStrings

def stitchPointsCalculator(stichprediction, stitches, number_of_players):  Array[Int] Int ={
  val stitchpoints = Array.ofDim[Int](number_of_players,1)
  for (i <- 0 to number_of_players-1) {
    if(stitches(i)-stitchprediction(i)==0 && stitches(i)!=0){
      stitchpoints(i)= stitches(i)*10 +20
    }
    if(sti@@tches(i)-stitchprediction(i)<0 && stitches(i)!=0){
      val punkteZuVielAbgezogen=stitches(i)+(stitches(i)-stitchprediction(i))
      stitchpoints(i)= punkteZuVielAbgezogen*10
    }
     
  }
}

def stitchPrediction(input: => String = readLine(),number_of_players:Int): Array[Int] = {
  val playerguess = Array.ofDim[Int](number_of_players)
  for (i <- 0 to number_of_players-1) {
    print(s"How many Stitches will you make player${i}?\n")
    try
      val stitchguess = input.toInt
      playerguess(i)=stitchguess
    catch
      case _: NumberFormatException =>
        println(Console.RED + "\nUngültige Eingabe! Bitte eine Zahl eingeben.\n" + Console.RESET)
  }
  playerguess
}

def stitchgame(number_of_players: Int, rounds: Int, playercards:Array[Array[String]]):Array[Int]={
  Array.ofDim[Int](number_of_players)
}

def game(): Int = {

  val cardarray= Array(
    "blue 1", "blue 2", "blue 3",
    "green 1", "green 2", "green 3",
    "red 1", "red 2", "red 3",
    "yellow 1", "yellow 2", "yellow 3"
  )
  val playercount = getPlayerCount()
  //val roundvalue = Array(20, 15, 12, 10 )
  val roundvalue = Array(4, 3, 2, 2 )
  val roundcount = roundvalue(playercount-3)

  for (i <- 1 until roundcount) {
        round(playercount,i,cardarray).foreach(println)
  }

  return 0
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.