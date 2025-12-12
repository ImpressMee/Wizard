package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView

class ScoreRoundCommandSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // MockView – minimal & deterministisch
  // ------------------------------------------------------------
  class MockView extends GameView {
    override def update(): Unit = ()

    // nicht relevant, aber notwendig
    override def readPlayerAmount(): Int = 3
    override def chooseTrump(): CardColor = CardColor.Red
    override def askPlayerAmount(): Unit = ()
    override def askHowManyTricks(p: Player): Unit = ()
    override def readPositiveInt(): Int = 1
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
  "ScoreRoundCommand" should {

    "execute score the round and return updated GameState" in {
      val view = new MockView
      val control = new GameControl(view)

      val state = GameState(
        amountOfPlayers = 3,
        players = List(
          Player(0, tricks = 1, predictedTricks = 1), // korrekt vorhergesagt
          Player(1, tricks = 0, predictedTricks = 1),
          Player(2, tricks = 0, predictedTricks = 0)
        ),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val cmd = ScoreRoundCommand(control, state)
      val result = cmd.execute()

      result.players.find(_.id == 0).get.totalPoints shouldBe 30
      result.players.forall(_.tricks == 0) shouldBe true
      result.players.forall(_.predictedTricks == 0) shouldBe true
    }

    "undo restores the previous GameState via memento" in {
      val view = new MockView
      val control = new GameControl(view)

      val original = GameState(
        amountOfPlayers = 3,
        players = List(
          Player(0, tricks = 1, predictedTricks = 1),
          Player(1),
          Player(2)
        ),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val cmd = ScoreRoundCommand(control, original)
      cmd.execute()

      val restored = cmd.undo()

      restored shouldBe original
      restored.players.find(_.id == 0).get.totalPoints shouldBe 0
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

      val cmd = ScoreRoundCommand(control, original)

      // last == gs
      val restored = cmd.undo()

      restored shouldBe original
    }
  }
}
