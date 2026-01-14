package de.htwg.wizard.model.modelComponent

import de.htwg.wizard.model.modelComponent.{CardColor, Deck, GameStateMemento, NormalCard, Player, Trick}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameStateMementoSpec extends AnyWordSpec with Matchers {

  "GameStateMemento" should {

    "store all provided values correctly" in {
      val players = List(Player(0), Player(1))
      val deck = Deck()
      val trick = Trick(Map(0 -> NormalCard(CardColor.Red, 5)))

      val m = GameStateMemento(
        amountOfPlayers = 2,
        players = players,
        deck = deck,
        currentRound = 3,
        totalRounds = 10,
        currentTrump = Some(CardColor.Blue),
        currentTrick = Some(trick)
      )

      m.amountOfPlayers shouldBe 2
      m.players shouldBe players
      m.deck shouldBe deck
      m.currentRound shouldBe 3
      m.totalRounds shouldBe 10
      m.currentTrump shouldBe Some(CardColor.Blue)
      m.currentTrick shouldBe Some(trick)
    }

    "support equality for identical state snapshots" in {
      val players = List(Player(0))
      val deck = Deck()

      val m1 = GameStateMemento(
        amountOfPlayers = 1,
        players = players,
        deck = deck,
        currentRound = 1,
        totalRounds = 5,
        currentTrump = None,
        currentTrick = None
      )

      val m2 = GameStateMemento(
        amountOfPlayers = 1,
        players = players,
        deck = deck,
        currentRound = 1,
        totalRounds = 5,
        currentTrump = None,
        currentTrick = None
      )

      m1 shouldBe m2
    }

    "not be equal if any field differs" in {
      val m1 = GameStateMemento(
        amountOfPlayers = 1,
        players = List(Player(0)),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 5,
        currentTrump = None,
        currentTrick = None
      )

      val m2 = m1.copy(currentRound = 2)

      m1 should not be m2
    }
  }
}
