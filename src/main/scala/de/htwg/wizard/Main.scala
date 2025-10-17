package de.htwg.wizard

import scala.io.StdIn._

// @author Justin-Jay Balaba
// @author Nikita Kusch
// test developer branch
// Test contribution
// TODO: var -> val machen.
// TODO: Speichere Prediction in eine Map mit dem Spieler
// TODO: Spielpunkte werden berechnen

//I am a chickennuggie

@main def hello(): Unit =
  val start = new start
  val number_of_players = start.getPlayerCount

  // Die rundenzahl gibt auch gleichzeitig die Anzahl der Karten an
  val runde = 1
  val trumpf = "Karte x,y"
  println(s"Es spielen $number_of_players Spieler mit.\n")

  // Start der runde:
  println(s"Runde: $runde")
  println(s"Trumpf ist: $trumpf\n")

  // Stich vorhersage:
  var prediction = -1
  for i <- 1 to number_of_players do
    prediction = -1
    while prediction < 0 do
      println("+-----------")
      println(s"Spieler $i, was ist deine Stich vorhersage?")
      try
        prediction = readLine().toInt
        if prediction <= runde then
          println(s"Spieler $i sagt $prediction Stiche vor")
        else
          println("Ungültige Eingabe! Bitte eine Zahl eingeben\n" +
            "! Die vorhersage darf nicht größer als die Rundenzahl sein !.\n")
          prediction = -1
      catch
        case _: NumberFormatException =>


  for i <- 1 to number_of_players do {
    println("\n-----------")
    println(s"| Player $i |")
    println("+-----------")
    println("| Karten: -")
    println("+-----------")
  }

