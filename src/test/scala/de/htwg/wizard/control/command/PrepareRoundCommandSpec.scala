package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*

class PrepareRoundCommandSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  val players =
    List(
      Player(0),
      Player(1),
      Player(2)
    )

  val initialState =
    GameState(
      amountOfPlayers = 3,
      players = players,
      deck = Deck(),          // wird ignoriert (neues Deck)
      currentRound = 0,
      totalRounds = 5,
      currentTrump = Some(CardColor.Red)
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "PrepareRoundCommand" should {

    "increment the round counter by exactly one" in {
      val result =
        PrepareRoundCommand.execute(initialState)

      result.currentRound shouldBe 1
    }

    "deal exactly currentRound cards to each player" in {
      val result =
        PrepareRoundCommand.execute(initialState)

      result.players.foreach { p =>
        p.hand.size shouldBe result.currentRound
      }
    }

    "deal unique cards to all players" in {
      val result =
        PrepareRoundCommand.execute(initialState)

      val allCards =
        result.players.flatMap(_.hand)

      allCards.distinct.size shouldBe allCards.size
    }

    "reduce the deck by the correct number of cards" in {
      val result =
        PrepareRoundCommand.execute(initialState)

      val dealtCards =
        result.players.size * result.currentRound

      result.deck.cards.size shouldBe
        Deck().cards.size - dealtCards
    }

    "reset the current trump to None" in {
      val result =
        PrepareRoundCommand.execute(initialState)

      result.currentTrump shouldBe None
    }

    "not mutate the original GameState" in {
      PrepareRoundCommand.execute(initialState)

      initialState.currentRound shouldBe 0
      initialState.players.foreach(_.hand shouldBe Nil)
      initialState.currentTrump shouldBe Some(CardColor.Red)
    }

    "return a new GameState instance" in {
      val result =
        PrepareRoundCommand.execute(initialState)

      result should not be theSameInstanceAs(initialState)
    }
  }
}
