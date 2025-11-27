package de.htwg.wizard

import de.htwg.wizard.control.*
import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyWordSpec with Matchers {

  class MockView extends GameView:
    override def askPlayerAmount(): Unit = ()
    override def readPlayerAmount(): Int = 1
    override def askHowManyTricks(p: Player): Unit = ()
    override def readPositiveInt(): Int = 0
    override def askPlayerCard(p: Player): Unit = ()
    override def readIndex(p: Player): Int = 0
    override def showTrickWinner(p: Player, c: Card): Unit = ()
    override def showRoundEvaluation(r: Int, players: List[Player]): Unit = ()
    override def showGameWinner(p: Player): Unit = ()
    override def showError(msg: String): Unit = ()
    override def showRoundInfo(r: Int, trump: CardColor, a: Int): Unit = ()
    override def showTrickStart(size: Int): Unit = ()
    override def update(): Unit = ()

  class MockController(view: GameView) extends GameControl(view):
    var runCalled = 0
    override def runGame(): Unit = runCalled += 1

  "startWizard" should {
    "create GameView, GameControl and call runGame" in {
      var createdView: MockView = null
      var createdController: MockController = null

      object TestMain:
        def startWizard(): Unit =
          createdView = new MockView()
          createdController = new MockController(createdView)
          createdController.runGame()

      TestMain.startWizard()

      createdView should not be null
      createdController should not be null
      createdController.runCalled shouldBe 1
    }
  }
}
