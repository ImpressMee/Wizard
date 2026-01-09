package de.htwg.wizard.control.event

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*

class GameEventSpec extends AnyWordSpec with Matchers {

  val state = GameState(
    amountOfPlayers = 2,
    players = List(Player(0), Player(1)),
    deck = Deck(),
    currentRound = 1,
    totalRounds = 5
  )

  "GameEvent implementations" should {

    "store state in RoundStarted" in {
      val e = RoundStarted(1, state)
      e.round shouldBe 1
      e.state shouldBe state
    }

    "store state in TrickStarted" in {
      val e = TrickStarted(2, state)
      e.n shouldBe 2
      e.state shouldBe state
    }

    "store state in TrickFinished" in {
      val e = TrickFinished(0, state)
      e.winnerId shouldBe 0
      e.state shouldBe state
    }

    "store state in RoundFinished" in {
      val e = RoundFinished(state)
      e.state shouldBe state
    }

    "store state in GameFinished" in {
      val winner = Player(1)
      val e = GameFinished(winner, state)

      e.winner shouldBe winner
      e.state shouldBe state
    }

    "store state in StateChanged" in {
      val e = StateChanged(state)
      e.state shouldBe state
    }

    "store state in PlayerAmountRequested" in {
      val e = PlayerAmountRequested(state)
      e.state shouldBe state
    }

    "store state in PredictionsRequested" in {
      val e = PredictionsRequested(state)
      e.state shouldBe state
    }

    "store state in TrickMoveRequested" in {
      val e = TrickMoveRequested(3, state)
      e.trickNr shouldBe 3
      e.state shouldBe state
    }
  }
}
