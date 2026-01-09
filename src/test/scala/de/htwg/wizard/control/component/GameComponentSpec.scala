package de.htwg.wizard.control.component

import de.htwg.wizard.component.game.GameComponent
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.input.*
import de.htwg.wizard.control.strategy.*
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.observer.Observer
import de.htwg.wizard.model.*

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

  def createGame(): (GameComponent, TestObserver) = {
    val game = new GameComponent(TestStrategy)
    val obs  = new TestObserver
    game.registerObserver(obs)
    (game, obs)
  }

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "GameComponent" should {

    "register observers and forward events to them" in {
      val (game, obs) = createGame()

      game.startGame()

      obs.events.head shouldBe a [PlayerAmountRequested]
    }

    "delegate PlayerAmountSelected to GameControl" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(PlayerAmountSelected(3))

      obs.events.exists(_.isInstanceOf[PredictionsRequested]) shouldBe true
    }

    "delegate PredictionsSubmitted to GameControl" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(PlayerAmountSelected(3))
      game.handleInput(PredictionsSubmitted(Map(0 -> 0, 1 -> 0, 2 -> 0)))

      obs.events.exists(_.isInstanceOf[TrickMoveRequested]) shouldBe true
    }

    "handle Undo by emitting StateChanged" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(Undo)

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe true
    }

    "handle Redo by emitting StateChanged" in {
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
  "GameComponent" should {

    "ignore TrickMovesSubmitted if no trick is active" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(PlayerAmountSelected(3))

      obs.events.exists(_.isInstanceOf[TrickMoveRequested]) shouldBe false
    }


    "delegate Undo to GameControl and emit StateChanged" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(Undo)

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe true
    }

    "delegate Redo to GameControl and emit StateChanged" in {
      val (game, obs) = createGame()

      game.startGame()
      game.handleInput(Redo)

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe true
    }
  }

}
