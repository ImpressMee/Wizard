package de.htwg.wizard.control

import de.htwg.wizard.control.controlComponents.GameControl
import de.htwg.wizard.control.controlComponents.strategy.TrickStrategy
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.control.event.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Deck, GameState, Player, Trick}

class GameControlSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Fake Strategy (deterministic)
  // ---------------------------------------------------------
  object TestStrategy extends TrickStrategy {
    override def winner(trick: Trick, trump: Option[CardColor]): (Int, Card) =
      trick.played.head

    override def isAllowedMove(card: Card, player: Player, trick: Trick): Boolean =
      true
  }

  // ---------------------------------------------------------
  // Event recorder
  // ---------------------------------------------------------
  class EventRecorder {
    var events: List[GameEvent] = Nil
    def notify(e: GameEvent): Unit = events ::= e
  }

  // ---------------------------------------------------------
  // Initial state
  // ---------------------------------------------------------
  def initialState =
    GameState(
      amountOfPlayers = 0,
      players = Nil,
      deck = Deck(),
      currentRound = 0,
      totalRounds = 0
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "GameControl" should {

    "emit PlayerAmountRequested on start" in {
      val recorder = new EventRecorder
      val control  = new GameControl(TestStrategy, recorder.notify)

      control.start(initialState)

      recorder.events.head shouldBe a [PlayerAmountRequested]
    }

    "transition to prediction phase after player amount submission" in {
      val recorder = new EventRecorder
      val control  = new GameControl(TestStrategy, recorder.notify)

      control.start(initialState)
      control.submitPlayerAmount(3)

      recorder.events.exists(_.isInstanceOf[PredictionsRequested]) shouldBe true
    }

    "transition to trick phase after predictions submission" in {
      val recorder = new EventRecorder
      val control  = new GameControl(TestStrategy, recorder.notify)

      control.start(initialState)
      control.submitPlayerAmount(3)
      control.submitPredictions(Map(0 -> 0, 1 -> 0, 2 -> 0))

      recorder.events.exists(_.isInstanceOf[TrickMoveRequested]) shouldBe true
    }

    "emit RoundFinished after last trick" in {
      val recorder = new EventRecorder
      val control  = new GameControl(TestStrategy, recorder.notify)

      // prepare minimal playable state
      val players =
        List(
          Player(0, hand = List(Card(CardColor.Red, 1))),
          Player(1, hand = List(Card(CardColor.Blue, 2)))
        )

      val state =
        GameState(
          amountOfPlayers = 2,
          players = players,
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.start(state)

      control.submitPredictions(Map(0 -> 0, 1 -> 0))
      control.playTrick(Map(0 -> 0, 1 -> 0))

      recorder.events.exists(_.isInstanceOf[RoundFinished]) shouldBe true
    }

    "emit GameFinished when final round is completed" in {
      val recorder = new EventRecorder
      val control  = new GameControl(TestStrategy, recorder.notify)

      val players =
        List(
          Player(0, hand = List(Card(CardColor.Red, 1)), totalPoints = 10),
          Player(1, hand = List(Card(CardColor.Blue, 2)), totalPoints = 0)
        )

      val state =
        GameState(
          amountOfPlayers = 2,
          players = players,
          deck = Deck(),
          currentRound = 1,
          totalRounds = 1
        )

      control.start(state)
      control.submitPredictions(Map(0 -> 0, 1 -> 0))
      control.playTrick(Map(0 -> 0, 1 -> 0))
      control.prepareNextRound()

      recorder.events.exists(_.isInstanceOf[GameFinished]) shouldBe true
    }

    "delegate isAllowedMove to the strategy" in {
      val recorder = new EventRecorder
      val control  = new GameControl(TestStrategy, recorder.notify)

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
  }
}
