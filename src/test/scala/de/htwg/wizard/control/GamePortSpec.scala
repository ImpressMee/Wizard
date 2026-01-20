package de.htwg.wizard.control

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.controlComponent.component.GameComponent
import de.htwg.wizard.control.controlComponent.GameControl
import de.htwg.wizard.control.controlComponent.strategy.StandardTrickStrategy
import de.htwg.wizard.persistence.json.FileIOJson
import de.htwg.wizard.model.modelComponent.*
import de.htwg.wizard.model.*

class GamePortSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Helper: frische GamePort-Instanz pro Test
  // ---------------------------------------------------------
  private def newGame(): GamePort = {
    val control =
      new GameControl(new StandardTrickStrategy, new FileIOJson)
    new GameComponent(control)
  }

  "GamePort implementation (GameComponent)" should {

    "allow observer registration" in {
      val game = newGame()
      var called = false

      game.registerObserver(_ => called = true)

      // kein Event ausgelöst → Observer nicht aufgerufen
      called shouldBe false
    }

    "initialize game and emit load availability" in {
      val game = newGame()
      noException shouldBe thrownBy(game.init())
    }

    "start a new game" in {
      val game = newGame()
      noException shouldBe thrownBy(game.startGame())
    }

    "delegate isAllowedMove correctly" in {
      val game = newGame()

      val state =
        GameState(
          amountOfPlayers = 1,
          players = List(
            Player(0, List(NormalCard(CardColor.Red, 5)))
          ),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      game.isAllowedMove(0, 0, state) shouldBe true
    }

    "support canSafelyExit flag" in {
      val game = newGame()
      game.canSafelyExit shouldBe true
    }
  }
}
