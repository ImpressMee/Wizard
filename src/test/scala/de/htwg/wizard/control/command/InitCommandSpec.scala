package de.htwg.wizard.control.command

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.control.GameControl
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView

class InitCommandSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // MockView: liefert deterministische Eingaben für doInitGame
  // ------------------------------------------------------------
  class MockView extends GameView {
    private var inputs = List(3) // gültige Spieleranzahl
    var errorShown = false

    override def askPlayerAmount(): Unit = ()

    override def readPlayerAmount(): Int = {
      val h = inputs.head
      inputs = inputs.tail
      h
    }

    override def showError(msg: String): Unit =
      errorShown = true

    // nicht benötigt in diesem Test
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
  // TESTS
  // ------------------------------------------------------------

  "InitCommand" should {

    "execute initialize the game, save state and return it" in {
      val view = new MockView
      val control = new GameControl(view)

      val cmd = InitCommand(control)
      val result = cmd.execute()

      // Rückgabewert
      result.amountOfPlayers shouldBe 3
      result.players.size shouldBe 3
      result.currentRound shouldBe 0

      // implizit getestet:
      // - doInitGame()
      // - saveState()
      // - last = s
    }

    "undo restore the previously initialized GameState via memento" in {
      val view = new MockView
      val control = new GameControl(view)

      val cmd = InitCommand(control)
      val initialized = cmd.execute()

      val undone = cmd.undo()

      // undo liefert exakt den zuletzt gespeicherten Zustand
      undone shouldBe initialized
    }
  }
}
