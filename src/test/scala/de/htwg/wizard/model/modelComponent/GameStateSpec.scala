package de.htwg.wizard.model.modelComponent

import de.htwg.wizard.model.modelComponent.{CardColor, Deck, GameState, Player}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameStateSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Hilfsobjekte
  // ---------------------------------------------------------

  val initialState =
    GameState(
      amountOfPlayers = 2,
      players = List(Player(0), Player(1)),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5,
      currentTrump = Some(CardColor.Red),
      currentTrick = None,
      completedTricks = 2
    )

  // ---------------------------------------------------------
  // createMemento
  // ---------------------------------------------------------

  "GameState.createMemento" should {

    "create a snapshot of the current game state" in {
      val memento = initialState.createMemento()

      memento.amountOfPlayers shouldBe initialState.amountOfPlayers
      memento.players shouldBe initialState.players
      memento.deck shouldBe initialState.deck
      memento.currentRound shouldBe initialState.currentRound
      memento.totalRounds shouldBe initialState.totalRounds
      memento.currentTrump shouldBe initialState.currentTrump
      memento.currentTrick shouldBe initialState.currentTrick
    }
  }

  // ---------------------------------------------------------
  // restore
  // ---------------------------------------------------------

  "GameState.restore" should {

    "restore a previous snapshot and undo later changes" in {
      val memento = initialState.createMemento()

      // veränderter Zustand
      val modifiedState =
        initialState.copy(
          currentRound = 3,
          completedTricks = 5,
          currentTrump = None
        )

      val restored = modifiedState.restore(memento)

      restored.amountOfPlayers shouldBe initialState.amountOfPlayers
      restored.players shouldBe initialState.players
      restored.deck shouldBe initialState.deck
      restored.currentRound shouldBe initialState.currentRound
      restored.totalRounds shouldBe initialState.totalRounds
      restored.currentTrump shouldBe initialState.currentTrump
      restored.currentTrick shouldBe initialState.currentTrick
    }

    "not modify fields that are not part of the memento" in {
      val memento = initialState.createMemento()

      val modified =
        initialState.copy(
          completedTricks = 99 // nicht im Memento enthalten
        )

      val restored = modified.restore(memento)

      restored.completedTricks shouldBe modified.completedTricks
    }
  }
}
