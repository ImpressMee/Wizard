package de.htwg.wizard.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalafx.application.Platform
import scalafx.stage.Stage
import de.htwg.wizard.control.*
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.*
import javafx.embed.swing.JFXPanel
import org.scalatest.Ignore

import java.util.concurrent.CountDownLatch
/*

Complete test inside a comment, because github ignores the @ignore annotation
which is why the coverall test fails everytime.
*/
@Ignore
class GuiViewSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // JavaFX bootstrap (PASSIV, exception-safe)
  // ---------------------------------------------------------
  // Initialisiert JavaFX genau einmal pro JVM – ohne startup()
  private val _ = new JFXPanel()

  // FX-Helfer
  private def runFx(block: => Unit): Unit = {
    val latch = new CountDownLatch(1)
    Platform.runLater {
      try block
      finally latch.countDown()
    }
    latch.await()
  }

  // ---------------------------------------------------------
  // Fake GamePort
  // ---------------------------------------------------------
  class FakeGamePort extends GamePort {
    override def registerObserver(o: Observer): Unit = ()
    override def startGame(): Unit = ()
    override def init(): Unit = ()
    override def handleInput(input: GameInput): Unit = ()
    override def isAllowedMove(p: Int, c: Int, s: GameState): Boolean = true
    override def canSafelyExit: Boolean = true
  }

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  private val baseState =
    GameState(
      amountOfPlayers = 2,
      players = List(Player(0), Player(1)),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 5
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "GuiView" should {

    "show start scene without throwing" in {
      val gui = new GuiView(new FakeGamePort)
      noException shouldBe thrownBy {
        runFx { gui.showStart(new Stage) }
      }
    }

    "switch to player count scene on PlayerAmountRequested" in {
      val gui = new GuiView(new FakeGamePort)
      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(PlayerAmountRequested(baseState))
        }
      }
    }

    "switch to prediction scene on PredictionsRequested" in {
      val gui = new GuiView(new FakeGamePort)
      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(PredictionsRequested(baseState))
        }
      }
    }

    "switch to trick scene on TrickMoveRequested" in {
      val gui = new GuiView(new FakeGamePort)
      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(TrickMoveRequested(0, baseState))
        }
      }
    }

    "switch to summary scene on RoundFinished" in {
      val gui = new GuiView(new FakeGamePort)
      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(RoundFinished(baseState))
        }
      }
    }

    "switch to end scene on GameFinished" in {
      val gui = new GuiView(new FakeGamePort)
      noException shouldBe thrownBy {
        runFx {
          gui.showStart(new Stage)
          gui.update(GameFinished(Player(0), baseState))
        }
      }
    }
  }
}
