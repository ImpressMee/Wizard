package de.htwg.wizard.view

import de.htwg.wizard.model.{Card, CardColor, Player}
import de.htwg.wizard.util.Observer

class MockView extends GameView {
  override def askPlayerAmount(): Unit = {}
  override def askNewStitches(player: Player): Unit = {}
  override def askPlayerCard(player: Player): Unit = {}
  override def showRoundInfo(round: Int, trump: CardColor, numberOfPlayers: Int): Unit = {}
  override def showPlayerCards(players: List[Player]): Unit = {}
  override def showStitchStart(): Unit = {}
  override def showStitchWinner(player: Player, winningCard: Card): Unit = {}
  override def showRoundEvaluation(round: Int, players: List[Player]): Unit = {}
  override def showGameWinner(player: Player): Unit = {}
  override def showError(msg: String): Unit = {}
  override def update(): Unit = {}
}
