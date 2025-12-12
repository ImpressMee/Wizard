package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView
import scala.util.{Try, Success}

class DetermineWinnerCommandSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // MockView: notwendig, um doDetermineWinner auszuführen
  // ------------------------------------------------------------
  class MockView extends GameView {
    var winnerShown = false

    override def showGameWinner(p: Player): Unit =
      winnerShown = true

    override def askPlayerAmount(): Unit = ()
    override def readPlayerAmount(): Try[Int]= Success(3)
    override def showError(msg: String): Unit = ()

    override def chooseTrump(): CardColor =
      throw new UnsupportedOperationException("not used in this test")

    override def showRoundInfo(r: Int, t: Option[CardColor], p: Int): Unit = ()
    override def askHowManyTricks(p: Player): Unit = ()
    override def readPositiveInt(): Int = 0
    override def askPlayerCard(p: Player): Unit = ()
    override def readIndex(p: Player): Int = 0
    override def showTrickStart(n: Int): Unit = ()
    override def showTrickWinner(p: Player, c: Card): Unit = ()
    override def showRoundEvaluation(r: Int, p: List[Player]): Unit = ()
  }

  // ------------------------------------------------------------
  // TESTS
  // ------------------------------------------------------------

  "DetermineWinnerCommand" should {

    "execute determine the winner and return the same GameState" in {
      val view = new MockView
      val control = new GameControl(view)

      val p1 = Player(0, Nil, totalPoints = 10)
      val p2 = Player(1, Nil, totalPoints = 20)

      val gs = GameState(
        amountOfPlayers = 2,
        players = List(p1, p2),
        deck = new Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val cmd = DetermineWinnerCommand(control, gs)
      val result = cmd.execute()

      result shouldBe gs
      view.winnerShown shouldBe true
    }

    "undo returns the unchanged GameState" in {
      val view = new MockView
      val control = new GameControl(view)

      val gs = GameState(
        amountOfPlayers = 1,
        players = List(Player(0)),
        deck = new Deck(),
        currentRound = 0,
        totalRounds = 0,
        currentTrump = None
      )

      val cmd = DetermineWinnerCommand(control, gs)
      cmd.undo() shouldBe gs
    }
  }
}
