package de.htwg.wizard.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalafx.application.Platform
import scalafx.stage.Stage
import de.htwg.wizard.control.{GameFinished, GameInput, GamePort, Observer, PlayerAmountRequested, PredictionsRequested, RoundFinished, TrickMoveRequested}
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.input.*
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardColor, Deck, GameState, Player}

import java.util.concurrent.CountDownLatch

class GuiViewSpec extends AnyWordSpec with Matchers {

  // JavaFX bootstrap (once)
  Platform.startup(() => ())

  // Run FX code synchronously
  def runFx(block: => Unit): Unit = {
    val latch = new CountDownLatch(1)
    Platform.runLater { block; latch.countDown() }
    latch.await()
  }

  // Fake GamePort
  class FakeGamePort extends GamePort {
    var started = false
    var lastInput: Option[GameInput] = None

    override def registerObserver(observer: Observer): Unit = ()
    override def startGame(): Unit = started = true
    override def handleInput(input: GameInput): Unit = lastInput = Some(input)
    override def isAllowedMove(pid: Int, idx: Int, state: GameState): Boolean = true
  }

  val baseState =
    GameState(
      amountOfPlayers = 2,
      players = List(Player(0), Player(1)),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  "GuiView" should {

    "show start scene without throwing" in {
      val game = new FakeGamePort
      val gui  = new GuiView(game)

      noException shouldBe thrownBy {
        runFx { gui.showStart(new Stage) }
      }
    }

    "switch to player count scene on PlayerAmountRequested" in {
      val game = new FakeGamePort
      val gui  = new GuiView(game)

      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(PlayerAmountRequested(baseState))
        }
      }
    }

    "switch to prediction scene on PredictionsRequested" in {
      val game = new FakeGamePort
      val gui  = new GuiView(game)

      val state =
        baseState.copy(
          players = List(
            Player(0, hand = List(Card(CardColor.Red, 1))),
            Player(1, hand = List(Card(CardColor.Blue, 2)))
          )
        )

      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(PredictionsRequested(state))
        }
      }
    }

    "switch to game board on TrickMoveRequested" in {
      val game = new FakeGamePort
      val gui  = new GuiView(game)

      val state =
        baseState.copy(
          players = List(
            Player(0, hand = List(Card(CardColor.Red, 1))),
            Player(1, hand = List(Card(CardColor.Blue, 2)))
          )
        )

      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(TrickMoveRequested(1, state))
        }
      }
    }

    "switch to summary on RoundFinished" in {
      val game = new FakeGamePort
      val gui  = new GuiView(game)

      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(RoundFinished(baseState))
        }
      }
    }

    "switch to end scene on GameFinished" in {
      val game = new FakeGamePort
      val gui  = new GuiView(game)

      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(GameFinished(Player(0), baseState))
        }
      }
    }
  }
}
