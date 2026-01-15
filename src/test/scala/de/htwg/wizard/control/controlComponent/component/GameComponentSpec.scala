package de.htwg.wizard.control.controlComponent.component

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.*
import de.htwg.wizard.control.controlComponent.GameControl
import de.htwg.wizard.model.modelComponent.{GameState, PlayerID}

// ---------------------------------------------------------
// Fake GameControl for delegation testing
// ---------------------------------------------------------
class FakeGameControl extends GameControl(null, null) {

  var initCalled = false
  var startCalledWith: Option[GameState] = None
  var submittedPlayerAmount: Option[Int] = None
  var predictionsSubmitted = false
  var trickPlayed = false
  var undoCalled = false
  var redoCalled = false
  var loadCalled = false
  var nextRoundCalled = false
  var isAllowedMoveCalled = false

  override def init(): Unit =
    initCalled = true

  override def start(state: GameState): Unit =
    startCalledWith = Some(state)

  override def submitPlayerAmount(n: Int): Unit =
    submittedPlayerAmount = Some(n)

  override def submitPredictions(p: Map[Int, Int]): Unit =
    predictionsSubmitted = true

  override def playTrick(m: Map[Int, Int]): Unit =
    trickPlayed = true

  override def prepareNextRound(): Unit =
    nextRoundCalled = true

  override def undo(): Unit =
    undoCalled = true

  override def redo(): Unit =
    redoCalled = true

  override def loadGame(): Unit =
    loadCalled = true

  override def isAllowedMove(
                              playerId: PlayerID,
                              cardIndex: Int,
                              state: GameState
                            ): Boolean = {
    isAllowedMoveCalled = true
    true
  }

  override def canSafelyExit: Boolean = true
}

class GameComponentSpec extends AnyWordSpec with Matchers {

  "GameComponent" should {

    "delegate init to GameControl" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.init()

      control.initCalled shouldBe true
    }

    "delegate startGame with empty GameState" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.startGame()

      control.startCalledWith shouldBe Some(GameState.empty)
    }

    "delegate PlayerAmountSelected to submitPlayerAmount" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.handleInput(PlayerAmountSelected(3))

      control.submittedPlayerAmount shouldBe Some(3)
    }

    "delegate PredictionsSubmitted to submitPredictions" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.handleInput(PredictionsSubmitted(Map(0 -> 1)))

      control.predictionsSubmitted shouldBe true
    }

    "delegate TrickMovesSubmitted to playTrick" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.handleInput(TrickMovesSubmitted(Map(0 -> 0)))

      control.trickPlayed shouldBe true
    }

    "delegate ContinueAfterRound to prepareNextRound" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.handleInput(ContinueAfterRound)

      control.nextRoundCalled shouldBe true
    }

    "delegate Undo and Redo" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.handleInput(Undo)
      game.handleInput(Redo)

      control.undoCalled shouldBe true
      control.redoCalled shouldBe true
    }

    "delegate LoadGame" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.handleInput(LoadGame)

      control.loadCalled shouldBe true
    }

    "delegate isAllowedMove to GameControl" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      val result =
        game.isAllowedMove(0, 0, GameState.empty)

      result shouldBe true
      control.isAllowedMoveCalled shouldBe true
    }

    "delegate canSafelyExit to GameControl" in {
      val control = new FakeGameControl
      val game    = new GameComponent(control)

      game.canSafelyExit shouldBe true
    }
  }
}
