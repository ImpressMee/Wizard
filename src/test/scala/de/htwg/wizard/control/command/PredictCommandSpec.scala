package de.htwg.wizard.control.command

import scala.util.{Try, Success}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView

class PredictCommandSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // MockView – deterministisch
  // ------------------------------------------------------------
  class MockView extends GameView {
    override def readPositiveInt(): Int = 1

    override def askHowManyTricks(p: Player): Unit = ()
    override def update(): Unit = ()

    // unused methods
    override def readPlayerAmount(): Try[Int] = Success(3)
    override def chooseTrump(): CardColor = CardColor.Red
    override def askPlayerAmount(): Unit = ()
    override def askPlayerCard(p: Player): Unit = ()
    override def readIndex(p: Player): Int = 0
    override def showError(msg: String): Unit = ()
    override def showTrickStart(n: Int): Unit = ()
    override def showTrickWinner(p: Player, c: Card): Unit = ()
    override def showRoundInfo(r: Int, t: Option[CardColor], n: Int): Unit = ()
    override def showRoundEvaluation(r: Int, p: List[Player]): Unit = ()
    override def showGameWinner(p: Player): Unit = ()
  }

  // ------------------------------------------------------------
  // Tests
  // ------------------------------------------------------------
  "PredictCommand" should {

    "execute set predictions, notify observers and return new GameState" in {
      val view = new MockView
      val control = new GameControl(view)

      val state = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val cmd = PredictCommand(control, state)
      val result = cmd.execute()

      result.players.forall(_.predictedTricks == 1) shouldBe true
    }

    "undo restores the previous GameState via memento" in {
      val view = new MockView
      val control = new GameControl(view)

      val original = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val cmd = PredictCommand(control, original)
      val afterPredict = cmd.execute()

      val restored = cmd.undo()

      restored shouldBe original
      restored.players.forall(_.predictedTricks == 0) shouldBe true
    }
  }

  "undo before execute returns the original GameState" in {
    val view = new MockView
    val control = new GameControl(view)

    val original = GameState(
      amountOfPlayers = 3,
      players = List(Player(0), Player(1), Player(2)),
      deck = Deck(),
      currentRound = 1,
      totalRounds = 1,
      currentTrump = None
    )

    val cmd = PredictCommand(control, original)

    // undo without execute -> last == gs
    val restored = cmd.undo()

    restored shouldBe original
  }

}
