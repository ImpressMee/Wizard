package de.htwg.wizard.control.controlComponent.component

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponent.GameControl
import de.htwg.wizard.control.controlComponent.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.*

class GameComponentSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Test Observer
  // ---------------------------------------------------------
  class TestObserver extends Observer {
    var events: List[GameEvent] = Nil
    override def update(event: GameEvent): Unit =
      events ::= event
  }

  // ---------------------------------------------------------
  // Deterministic Strategy
  // ---------------------------------------------------------
  object TestStrategy extends TrickStrategy {
    override def winner(trick: Trick, trump: Option[CardColor]): (Int, Card) =
      trick.played.head

    override def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
      true
  }

  // ---------------------------------------------------------
  // Helper
  // ---------------------------------------------------------
  private def createGame(): (GameComponent, TestObserver) = {
    val model: ModelInterface = new ModelComponent()
    val control = new GameControl(model, TestStrategy)

    val game = new GameComponent(control)
    val obs  = new TestObserver

    game.registerObserver(obs)
    (game, obs)
  }

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "GameComponent" should {

    "emit PlayerAmountRequested on startGame" in {
      val (game, obs) = createGame()

      game.startGame()

      obs.events.exists(_.isInstanceOf[PlayerAmountRequested]) shouldBe true
    }

    "forward PlayerAmountSelected to GameControl" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(PlayerAmountSelected(3))

      obs.events.exists(_.isInstanceOf[PredictionsRequested]) shouldBe true
    }

    "forward PredictionsSubmitted to GameControl" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(PlayerAmountSelected(3))
      game.handleInput(PredictionsSubmitted(Map(0 -> 0, 1 -> 0, 2 -> 0)))

      obs.events.exists(_.isInstanceOf[TrickMoveRequested]) shouldBe true
    }

    "emit StateChanged on Undo" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(Undo)

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe true
    }

    "emit StateChanged on Redo" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(Redo)

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe true
    }

    "delegate isAllowedMove to GameControl" in {
      val (game, _) = createGame()

      val state =
        GameState(
          amountOfPlayers = 1,
          players = List(Player(0, hand = List(Card(CardColor.Red, 1)))),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1,
          currentTrick = Some(Trick(Map.empty))
        )

      game.isAllowedMove(0, 0, state) shouldBe true
    }
  }
}
