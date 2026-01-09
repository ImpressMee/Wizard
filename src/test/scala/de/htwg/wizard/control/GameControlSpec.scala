package de.htwg.wizard.control

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.event.*
import de.htwg.wizard.model.*

class GameControlSpec extends AnyWordSpec with Matchers {

  "GameControl" should {

    "start the game and emit initial events" in {
      val control = new GameControl()
      val obs = new RecordingObserver

      control.runGame(obs)

      // mindestens ein StateChanged
      obs.events.exists(_.isInstanceOf[StateChanged]) shouldBe true

      // und eine Aufforderung zur Spieleranzahl
      obs.events.exists(_.isInstanceOf[PlayerAmountRequested]) shouldBe true
    }

    "initialize players and prepare first round after submitting player amount" in {
      val control = new GameControl()
      val obs = new RecordingObserver

      control.runGame(obs)
      control.submitPlayerAmount(3)

      val states =
        obs.events.collect { case StateChanged(gs) => gs }

      states.exists(_.players.size == 3) shouldBe true
      states.exists(_.currentRound == 1) shouldBe true
    }

    "request predictions after round preparation" in {
      val control = new GameControl()
      val obs = new RecordingObserver

      control.runGame(obs)
      control.submitPlayerAmount(3)

      obs.events.exists(_.isInstanceOf[PredictionsRequested]) shouldBe true
    }

    "apply predictions and emit updated state" in {
      val control = new GameControl()
      val obs = new RecordingObserver

      control.runGame(obs)
      control.submitPlayerAmount(3)

      control.submitPredictions(Map(0 -> 1, 1 -> 1, 2 -> 1))

      val states =
        obs.events.collect { case StateChanged(gs) => gs }

      states.exists { gs =>
        gs.players.forall(_.predictedTricks == 1)
      } shouldBe true
    }

    "support undo and restore a previous state" in {
      val control = new GameControl()
      val obs = new RecordingObserver

      control.runGame(obs)
      control.submitPlayerAmount(3)

      val statesBeforeUndo =
        obs.events.collect { case StateChanged(gs) => gs }

      control.undo()

      val statesAfterUndo =
        obs.events.collect { case StateChanged(gs) => gs }

      statesAfterUndo.size should be > statesBeforeUndo.size
    }

    "continue after round and emit a new phase event" in {
      val control = new GameControl()
      val obs = new RecordingObserver

      control.runGame(obs)
      control.submitPlayerAmount(3)

      control.continueAfterRound()

      obs.events.exists {
        case PredictionsRequested(_) => true
        case PlayerAmountRequested(_) => true
        case _ => false
      } shouldBe true
    }
  }
}
