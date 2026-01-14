package de.htwg.wizard.model.modelComponent

import de.htwg.wizard.model.modelComponent.Card

type PlayerID = Int // Alias for better readability
case class Trick(played: Map[PlayerID, Card])

