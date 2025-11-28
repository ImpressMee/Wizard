package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStatePhaseSpec extends AnyWordSpec with Matchers {

  // -------------------------------------------------------------------------
  // DummyView – MUSS GameView erweitern
  // -------------------------------------------------------------------------

  class DummyView extends GameView {
    override def askPlayerAmount(): Unit = ()
    override def readPlayerAmount(): Int = 3

    override def askHowManyTricks(p: Player): Unit = ()
    override def readPositiveInt(): Int = 0

    override def askPlayerCard(p: Player): Unit = ()
    override def readIndex(p: Player): Int = 0

    override def showTrickWinner(player: Player, card: Card): Unit = ()
    override def showRoundEvaluation(round: Int, players: List[Player]): Unit = ()
    override def showGameWinner(player: Player): Unit = ()

    override def showError(msg: String): Unit = ()

    override def chooseTrump(): CardColor = CardColor.Red
    override def showRoundInfo(r: Int, trump: Option[CardColor], p: Int): Unit = ()

    override def update(): Unit = ()
  }

  // -------------------------------------------------------------------------
  // MockControl – track all calls
  // -------------------------------------------------------------------------

  class MockControl(
                     initState: GameState,
                     playersAfterInit: List[Player] = List(Player(0), Player(1), Player(2))
                   ) extends GameControl(new DummyView()) {

    var initCalled = false
    var prepareCalled = false
    var predictCalled = false
    var trickCalled = List.empty[Int]
    var scoreCalled = false
    var finishCalled = false

    private var current = initState

    override private[control] def initGame(): GameState =
      initCalled = true
      current = current.copy(players = playersAfterInit)
      current

    override private[control] def prepareNextRound(gs: GameState): GameState =
      prepareCalled = true
      val g2 = gs.copy(currentRound = gs.currentRound + 1)
      current = g2
      g2

    override private[control] def predictTricks(gs: GameState): GameState =
      predictCalled = true
      current = gs
      gs

    override private[control] def playOneTrick(n: Int, gs: GameState): GameState =
      trickCalled = trickCalled :+ n
      current = gs
      gs

    override private[control] def scoreRound(gs: GameState): GameState =
      scoreCalled = true
      current = gs
      gs

    override private[control] def finishGame(gs: GameState): Unit =
      finishCalled = true
  }

  // -------------------------------------------------------------------------
  // Test-Zustand
  // -------------------------------------------------------------------------

  val baseState =
    GameState(
      amountOfPlayers = 3,
      players = List(Player(0), Player(1), Player(2)),
      deck = new Deck(),
      currentRound = 0,
      totalRounds = 5,
      currentTrump = None
    )

  // -------------------------------------------------------------------------
  // TESTS
  // -------------------------------------------------------------------------

  "InitState" should {
    "call initGame and prepareNextRound and move to PredictState" in {
      val ctrl = new MockControl(baseState)
      val (next, s2) = InitState.run(ctrl, baseState)

      ctrl.initCalled shouldBe true
      ctrl.prepareCalled shouldBe true
      next shouldBe PredictState
      s2.currentRound shouldBe 1
    }
  }

  "PrepareRoundState" should {

    "call prepareNextRound exactly once" in {
      val ctrl = new MockControl(baseState)
      PrepareRoundState.run(ctrl, baseState)
      ctrl.prepareCalled shouldBe true
    }

    "return PredictState when currentRound < totalRounds" in {
      val s = baseState.copy(currentRound = 1)
      val ctrl = new MockControl(s)
      val (next, _) = PrepareRoundState.run(ctrl, s)
      next shouldBe PredictState
    }

    "return PredictState when currentRound == totalRounds" in {
      val s = baseState.copy(currentRound = 5)
      val ctrl = new MockControl(s)
      val (next, _) = PrepareRoundState.run(ctrl, s)
      next shouldBe FinishState
    }

    "return FinishState when currentRound > totalRounds" in {
      val s = baseState.copy(currentRound = 6)
      val ctrl = new MockControl(s)
      val (next, _) = PrepareRoundState.run(ctrl, s)
      next shouldBe FinishState
    }
  }

  "PredictState" should {
    "call predictTricks and go to TrickState(1)" in {
      val ctrl = new MockControl(baseState)
      val (next, s2) = PredictState.run(ctrl, baseState)

      ctrl.predictCalled shouldBe true
      next shouldBe TrickState(1)
      s2 shouldBe baseState
    }
  }

  "TrickState" should {

    "go to ScoreState when n > hand size" in {
      val gs = baseState.copy(players = List(Player(0, hand = List())))
      val ctrl = new MockControl(gs)

      val (next, s2) = TrickState(1).run(ctrl, gs)

      next shouldBe ScoreState
      ctrl.trickCalled shouldBe empty
      s2 shouldBe gs
    }

    "call playOneTrick when n <= hand size" in {
      val gs = baseState.copy(players = List(Player(0, hand = List(NormalCard(CardColor.Red, 5)))))
      val ctrl = new MockControl(gs)

      val (next, s2) = TrickState(1).run(ctrl, gs)

      ctrl.trickCalled shouldBe List(1)
      next shouldBe TrickState(2)
      s2 shouldBe gs
    }

    "correctly increment trick number" in {
      val gs = baseState.copy(players = List(Player(0, hand = List.fill(2)(NormalCard(CardColor.Red, 5)))))
      val ctrl = new MockControl(gs)

      val (next, _) = TrickState(2).run(ctrl, gs)
      next shouldBe TrickState(3)
    }
  }

  "ScoreState" should {
    "call scoreRound and return PrepareRoundState" in {
      val ctrl = new MockControl(baseState)
      val (next, s2) = ScoreState.run(ctrl, baseState)

      ctrl.scoreCalled shouldBe true
      next shouldBe PrepareRoundState
      s2 shouldBe baseState
    }
  }

  "FinishState" should {
    "call finishGame and return null as next state" in {
      val ctrl = new MockControl(baseState)
      val (next, s2) = FinishState.run(ctrl, baseState)

      ctrl.finishCalled shouldBe true
      next shouldBe null
      s2 shouldBe baseState
    }
  }
}
