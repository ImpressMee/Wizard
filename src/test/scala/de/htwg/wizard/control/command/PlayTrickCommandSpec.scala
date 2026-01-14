package de.htwg.wizard.control.command

import de.htwg.wizard.control.controlComponents.command.PlayTrickCommand
import de.htwg.wizard.control.controlComponents.strategy.TrickStrategy
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Deck, GameState, Player, Trick}

class PlayTrickCommandSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Fake Strategy: always lets player 0 win
  // ---------------------------------------------------------
  object TestStrategy extends TrickStrategy {
    override def winner(trick: Trick, trump: Option[CardColor]): (Int, Card) =
      trick.played.head

    override def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
      true
  }

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  val cardA = Card(CardColor.Red, 1)
  val cardB = Card(CardColor.Blue, 2)
  val cardC = Card(CardColor.Green, 3)
  val cardD = Card(CardColor.Yellow, 4)

  val players =
    List(
      Player(0, hand = List(cardA, cardB), tricks = 0),
      Player(1, hand = List(cardC, cardD), tricks = 0)
    )

  val initialState =
    GameState(
      amountOfPlayers = 2,
      players = players,
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5,
      currentTrump = None,
      currentTrick = Some(Trick(Map.empty))
    )

  val moves = Map(
    0 -> 0, // Player 0 plays cardA
    1 -> 1  // Player 1 plays cardD
  )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "PlayTrickCommand" should {

    "remove the played cards from each player's hand" in {
      val result =
        PlayTrickCommand(moves, TestStrategy).execute(initialState)

      result.players.find(_.id == 0).get.hand shouldBe List(cardB)
      result.players.find(_.id == 1).get.hand shouldBe List(cardC)
    }

    "assign exactly one trick to the winning player" in {
      val result =
        PlayTrickCommand(moves, TestStrategy).execute(initialState)

      result.players.find(_.id == 0).get.tricks shouldBe 1
      result.players.find(_.id == 1).get.tricks shouldBe 0
    }

    "reset currentTrick to None after execution" in {
      val result =
        PlayTrickCommand(moves, TestStrategy).execute(initialState)

      result.currentTrick shouldBe None
    }

    "not mutate the original GameState" in {
      PlayTrickCommand(moves, TestStrategy).execute(initialState)

      initialState.players.foreach(_.tricks shouldBe 0)
      initialState.players.head.hand shouldBe List(cardA, cardB)
      initialState.currentTrick.isDefined shouldBe true
    }

    "return a new GameState instance" in {
      val result =
        PlayTrickCommand(moves, TestStrategy).execute(initialState)

      result should not be theSameInstanceAs(initialState)
    }
  }
}
