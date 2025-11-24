package de.htwg.wizard.model

type PlayerID = Int // Alias for better readability
case class Trick(played: Map[PlayerID, Card])

