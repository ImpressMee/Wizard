package de.htwg.wizard.control.controlComponent

import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponent.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameControlSpec extends AnyWordSpec with Matchers {

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
  // Event recorder
  // ---------------------------------------------------------
  class EventRecorder {
    var events: List[GameEvent] = Nil
    def notify(e: GameEvent): Unit = events ::= e
  }

  // ---------------------------------------------------------
  // Helper
  // ---------------------------------------------------------
  private def newControl(recorder: EventRecorder): GameControl = {
    val model: ModelInterface = new ModelComponent()
    new GameControl(model, TestStrategy, recorder.notify)
  }

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "GameControl" should {

    "emit PlayerAmountRequested on start" in {
      val recorder = new EventRecorder
      val control  = newControl(recorder)

      control.start(GameState.empty)

      recorder.events.exists(_.isInstanceOf[PlayerAmountRequested]) shouldBe true
    }

    "transition to prediction phase after player amount submission" in {
      val recorder = new EventRecorder
      val control  = newControl(recorder)

      control.start(GameState.empty)
      control.submitPlayerAmount(3)

      recorder.events.exists(_.isInstanceOf[PredictionsRequested]) shouldBe true
    }

    "transition to trick phase after predictions submission" in {
      val recorder = new EventRecorder
      val control  = newControl(recorder)

      control.start(GameState.empty)
      control.submitPlayerAmount(3)
      control.submitPredictions(Map(0 -> 0, 1 -> 0, 2 -> 0))

      recorder.events.exists(_.isInstanceOf[TrickMoveRequested]) shouldBe true
    }

    "emit RoundFinished after last trick" in {
      val recorder = new EventRecorder
      val control  = newControl(recorder)

      control.start(GameState.empty)
      control.submitPlayerAmount(2)
      control.submitPredictions(Map(0 -> 0, 1 -> 0))

      control.playTrick(Map(0 -> 0, 1 -> 0))

      recorder.events.exists(_.isInstanceOf[RoundFinished]) shouldBe true
    }



    "delegate isAllowedMove to the strategy" in {
      val recorder = new EventRecorder
      val control  = newControl(recorder)

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
