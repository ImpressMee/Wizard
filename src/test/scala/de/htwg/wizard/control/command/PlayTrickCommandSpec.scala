package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.control.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView

class PlayTrickCommandSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // MockView: minimal, nur für doPlayOneTrick nötig
  // ------------------------------------------------------------
  class MockView extends GameView {
    override def askPlayerAmount(): Unit = ()
    override def readPlayerAmount(): Int = 0
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
    override def showGameWinner(p: Player): Unit = ()
  }

  // ------------------------------------------------------------
  // Deterministische Strategy (kein Zufall!)
  // ------------------------------------------------------------
  class FixedWinnerStrategy extends TrickStrategy {
    override def winner(
                         trick: Trick,
                         trump: Option[CardColor]
                       ): (Int, Card) =
      trick.played.head
  }

  // ------------------------------------------------------------
  // TESTS
  // ------------------------------------------------------------

  "PlayTrickCommand" should {

    "execute play one trick and store last state" in {
      val view = new MockView
      val strategy = new FixedWinnerStrategy
      val control = new GameControl(view, strategy)

      val p0 = Player(0, List(NormalCard(CardColor.Red, 5)))
      val p1 = Player(1, List(NormalCard(CardColor.Blue, 7)))

      val gs = GameState(
        amountOfPlayers = 2,
        players = List(p0, p1),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val cmd = PlayTrickCommand(control, 1, gs)
      val result = cmd.execute()

      // Rückgabewert
      result.players.map(_.hand.size) shouldBe List(0, 0)

      // Gewinner hat einen Stich
      result.players.exists(_.tricks == 1) shouldBe true
    }

    "undo restores the previous GameState via memento" in {
      val view = new MockView
      val strategy = new FixedWinnerStrategy
      val control = new GameControl(view, strategy)

      val p0 = Player(0, List(NormalCard(CardColor.Red, 5)))
      val p1 = Player(1, List(NormalCard(CardColor.Blue, 7)))

      val gs = GameState(
        amountOfPlayers = 2,
        players = List(p0, p1),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val cmd = PlayTrickCommand(control, 1, gs)
      cmd.execute()

      val undone = cmd.undo()

      // undo() liefert exakt den vorherigen Zustand zurück
      undone shouldBe gs
    }
  }
}
