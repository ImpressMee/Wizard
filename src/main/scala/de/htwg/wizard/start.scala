package de.htwg.wizard

import scala.io.StdIn._

class start:
  // Fragt die Spieleranzahl ab (zwischen 3 und 6) und gibt sie zurück
  def getPlayerCount: Int =
    var number_of_players = 0
    // Schleife: wiederhole, bis gültige Eingabe (mind. 3 Spieler, max 6)
    while number_of_players < 3 || number_of_players > 6 do
      print("Wie viele Spieler machen mit? (3-6): ")
      try
        number_of_players = readLine().toInt
        if number_of_players < 3 || number_of_players > 6 then
          println("Falsche Anzahl! Bitte erneut versuchen.\n")
      catch
        case _: NumberFormatException =>
          println("Ungültige Eingabe! Bitte eine Zahl eingeben.\n")

    number_of_players // Rückgabewert (außerhalb der Schleife)
