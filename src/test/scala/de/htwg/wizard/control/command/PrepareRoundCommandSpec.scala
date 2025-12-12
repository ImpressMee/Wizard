package de.htwg.wizard.control.command

import scala.util.{Try, Success}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView

class PrepareRoundCommandSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // MockView – minimal & deterministisch
  // ------------------------------------------------------------
  class MockView extends GameView {
    override def chooseTrump(): CardColor = CardColor.Red
    override def update(): Unit = ()

    // nicht relevant für diesen Command
    override def readPlayerAmount(): Try[Int] = Success(3)
    override def readPositiveInt(): Int = 1
    override def askPlayerAmount(): Unit = ()
    override def askHowManyTricks(p: Player): Unit = ()
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
  "PrepareRoundCommand" should {

    "execute prepare the next round and return updated GameState" in {
      val view = new MockView
      val control = new GameControl(view)

      val state = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = Deck(),              // frisches Deck wird intern erzeugt
        currentRound = 0,
        totalRounds = 2,
        currentTrump = None
      )

      val cmd = PrepareRoundCommand(control, state)
      val result = cmd.execute()

      result.currentRound shouldBe 1
      result.players.forall(_.hand.nonEmpty) shouldBe true
    }

    "undo restores the previous GameState via memento" in {
      val view = new MockView
      val control = new GameControl(view)

      val original = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = Deck(),
        currentRound = 0,
        totalRounds = 2,
        currentTrump = None
      )

      val cmd = PrepareRoundCommand(control, original)
      val prepared = cmd.execute()

      val restored = cmd.undo()

      restored shouldBe original
      restored.currentRound shouldBe 0
      restored.players.forall(_.hand.isEmpty) shouldBe true
    }
  }
}
