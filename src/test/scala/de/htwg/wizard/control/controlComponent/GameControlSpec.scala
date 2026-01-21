package de.htwg.wizard.control.controlComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponent.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.*
import de.htwg.wizard.persistence.FileIO

class GameControlSpec extends AnyWordSpec with Matchers {

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
  // Dummy FileIO
  // ---------------------------------------------------------
  class DummyFileIO extends FileIO {
    var saved: List[GameState] = Nil
    override def save(state: GameState): Unit =
      saved ::= state
    override def load(): GameState =
      GameState.empty
    override def hasSave: Boolean = false
  }

  // ---------------------------------------------------------
  // Helper
  // ---------------------------------------------------------
  private def create(): (GameControl, TestObserver, DummyFileIO) = {
    val fileIO = new DummyFileIO
    val control = new GameControl(TestStrategy, fileIO)
    val obs = new TestObserver
    control.registerObserver(obs)
    (control, obs, fileIO)
  }

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "GameControl" should {

    "emit GameLoadAvailable on init" in {
      val (control, obs, _) = create()

      control.init()

      obs.events.exists(_.isInstanceOf[GameLoadAvailable]) shouldBe true
    }

    "emit PlayerAmountRequested on start" in {
      val (control, obs, _) = create()

      control.start(GameState.empty)

      obs.events.exists(_.isInstanceOf[PlayerAmountRequested]) shouldBe true
    }

    "initialize game after submitting player amount" in {
      val (control, obs, _) = create()

      control.start(GameState.empty)
      control.submitPlayerAmount(3)

      obs.events.exists(_.isInstanceOf[PredictionsRequested]) shouldBe true
    }

    "request trick move after predictions" in {
      val (control, obs, _) = create()

      control.start(GameState.empty)
      control.submitPlayerAmount(3)
      control.submitPredictions(Map(0 -> 0, 1 -> 0, 2 -> 0))

      obs.events.exists(_.isInstanceOf[TrickMoveRequested]) shouldBe true
    }

    "allow undo and emit StateChanged" in {
      val (control, obs, _) = create()

      control.start(GameState.empty)
      control.submitPlayerAmount(3)
      control.undo()

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe true
    }

    "allow redo and emit StateChanged" in {
      val (control, obs, _) = create()

      control.start(GameState.empty)
      control.submitPlayerAmount(3)
      control.undo()
      control.redo()

      obs.events.count(_.isInstanceOf[StateChanged]) shouldBe 2
    }

    "delegate isAllowedMove to the strategy" in {
      val (control, _, _) = create()

      val state =
        GameState(
          amountOfPlayers = 1,
          players = List(Player(0, hand = List(Card(CardColor.Red, 1)))),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1,
          currentTrick = Some(Trick(Map.empty))
        )

      control.isAllowedMove(0, 0, state) shouldBe true
    }

    "disallow unsafe exit during predict or trick phase" in {
      val (control, _, _) = create()

      control.start(GameState.empty)
      control.submitPlayerAmount(3)

      control.canSafelyExit shouldBe false
    }

    "load game and fire correct phase" in {
      val (control, obs, fileIO) = create()

      control.loadGame()

      obs.events.nonEmpty shouldBe true
    }

    "enter ScoreState when all players have no cards left after a trick" in {
      val (control, obs, _) = create()

      val state =
        GameState(
          amountOfPlayers = 2,
          players = List(
            Player(0, hand = List(Card(CardColor.Red, 1))),
            Player(1, hand = List(Card(CardColor.Blue, 2)))
          ),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.start(state)
      control.submitPredictions(Map(0 -> 0, 1 -> 0))
      control.playTrick(Map(0 -> 0, 1 -> 0))

      obs.events.exists(_.isInstanceOf[RoundFinished]) shouldBe true
    }

    "enter FinishState after last round" in {
      val (control, obs, _) = create()

      val state =
        GameState(
          amountOfPlayers = 1,
          players = List(Player(0)),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.start(state)
      control.prepareNextRound()

      obs.events.exists(_.isInstanceOf[GameFinished]) shouldBe true
    }

    "continueAfterRound returns to PredictState when rounds remain" in {
      val (control, obs, _) = create()

      val state =
        GameState(
          amountOfPlayers = 2,
          players = List(Player(0), Player(1)),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 3
        )

      control.start(state)
      control.continueAfterRound()

      obs.events.exists(_.isInstanceOf[PredictionsRequested]) shouldBe true
    }

    "ignore undo when no history exists" in {
      val (control, obs, _) = create()

      control.start(GameState.empty)
      control.undo()

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe false
    }

    "ignore redo when no future exists" in {
      val (control, obs, _) = create()

      control.start(GameState.empty)
      control.redo()

      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe false
    }

    "isAllowedMove returns false for invalid player or card index" in {
      val (control, _, _) = create()

      val state =
        GameState(
          amountOfPlayers = 1,
          players = List(Player(0, hand = List(Card(CardColor.Red, 1)))),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.isAllowedMove(99, 0, state) shouldBe false
      control.isAllowedMove(0, 99, state) shouldBe false
    }

    "throw IllegalStateException when currentState is accessed before start" in {
      val (control, _, _) = create()

      an[IllegalStateException] shouldBe thrownBy {
        control.submitPredictions(Map.empty)
      }
    }

    "stay in TrickState when players still have cards after a trick" in {
      val (control, obs, _) = create()

      val state =
        GameState(
          amountOfPlayers = 2,
          players = List(
            Player(0, hand = List(Card(CardColor.Red, 1), Card(CardColor.Red, 2))),
            Player(1, hand = List(Card(CardColor.Blue, 3), Card(CardColor.Blue, 4)))
          ),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.start(state)
      control.submitPredictions(Map(0 -> 0, 1 -> 0))
      control.playTrick(Map(0 -> 0, 1 -> 0))

      obs.events.exists {
        case TrickMoveRequested(_, _) => true
        case _ => false
      } shouldBe true
    }

    "enter InitState when loading a state without players" in {
      val (control, obs, _) = create()

      control.loadGame()

      obs.events.exists(_.isInstanceOf[PlayerAmountRequested]) shouldBe true
    }

    "allow safe exit in ScoreState and FinishState" in {
      val (control, _, _) = create()

      val state =
        GameState(
          amountOfPlayers = 1,
          players = List(Player(0)),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.start(state)
      control.prepareNextRound()

      control.canSafelyExit shouldBe true
    }

    "continueAfterRound enters FinishState when no rounds remain" in {
      val (control, obs, _) = create()

      val state =
        GameState(
          amountOfPlayers = 1,
          players = List(Player(0)),
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.start(state)
      control.continueAfterRound()

      obs.events.exists(_.isInstanceOf[GameFinished]) shouldBe true
    }

  }
}
