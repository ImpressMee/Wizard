package de.htwg.wizard.control
import scala.util.{Try, Success}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView

class GameStatePhaseSpec extends AnyWordSpec with Matchers {

  // ============================================================
  // MockView – minimal & deterministisch
  // ============================================================
  class MockView extends GameView {
    override def readPlayerAmount(): Try[Int] = Success(3)
    override def chooseTrump(): CardColor = CardColor.Red
    override def readPositiveInt(): Int = 1
    override def readIndex(p: Player): Int = 0

    override def askPlayerAmount(): Unit = ()
    override def askHowManyTricks(p: Player): Unit = ()
    override def askPlayerCard(p: Player): Unit = ()
    override def showError(msg: String): Unit = ()
    override def showTrickStart(n: Int): Unit = ()
    override def showTrickWinner(p: Player, c: Card): Unit = ()
    override def showRoundInfo(r: Int, t: Option[CardColor], n: Int): Unit = ()
    override def showRoundEvaluation(r: Int, p: List[Player]): Unit = ()
    override def showGameWinner(p: Player): Unit = ()
    override def update(): Unit = ()
  }

  // ============================================================
  // INIT STATE
  // ============================================================
  "InitState" should {

    "initialize game and move to PredictState" in {
      val control = new GameControl(new MockView)

      val start = GameState(
        0, Nil, Deck(), 0, 0, None
      )

      val (next, state) = InitState.run(control, start)

      next shouldBe Some(PredictState)
      state.amountOfPlayers shouldBe 3
      state.players.size shouldBe 3
      state.currentRound shouldBe 1
    }
  }

  // ============================================================
  // PREPARE ROUND STATE
  // ============================================================
  "PrepareRoundState" should {

    "move to PredictState if rounds remain" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        3,
        List(Player(0), Player(1), Player(2)),
        Deck(),
        currentRound = 0,
        totalRounds = 2,
        currentTrump = None
      )

      val (next, s2) = PrepareRoundState.run(control, state)

      next shouldBe Some(PredictState)
      s2.currentRound shouldBe 1
    }

    "move to FinishState if last round reached" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        3,
        List(Player(0), Player(1), Player(2)),
        Deck(),
        currentRound = 0,
        totalRounds = 1,
        currentTrump = None
      )

      val (next, _) = PrepareRoundState.run(control, state)

      next shouldBe Some(FinishState)
    }
  }

  // ============================================================
  // PREDICT STATE
  // ============================================================
  "PredictState" should {

    "set predictions and move to TrickState(1)" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        3,
        List(Player(0), Player(1), Player(2)),
        Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val (next, s2) = PredictState.run(control, state)

      next shouldBe Some(TrickState(1))
      s2.players.forall(_.predictedTricks == 1) shouldBe true
    }
  }

  // ============================================================
  // TRICK STATE
  // ============================================================
  "TrickState" should {

    "repeat same trick number on illegal move" in {
      val view = new MockView {
        override def readIndex(p: Player): Int =
          if p.id == 1 then 1 else 0 // illegal follow suit
      }
      val control = new GameControl(view)

      val players = List(
        Player(0, List(NormalCard(CardColor.Red, 5))),
        Player(1, List(
          NormalCard(CardColor.Red, 3),
          NormalCard(CardColor.Blue, 7)
        )),
        Player(2, List(NormalCard(CardColor.Red, 9)))
      )

      val state = GameState(
        3, players, Deck(), 1, 1, None
      )

      val (next, _) = TrickState(1).run(control, state)

      next shouldBe Some(TrickState(1))
    }

    "move to next TrickState if cards remain" in {
      val control = new GameControl(new MockView)

      val players = List(
        Player(0, List(NormalCard(CardColor.Red, 5), NormalCard(CardColor.Red, 6))),
        Player(1, List(NormalCard(CardColor.Red, 7), NormalCard(CardColor.Red, 8))),
        Player(2, List(NormalCard(CardColor.Red, 9), NormalCard(CardColor.Red, 10)))
      )

      val state = GameState(
        3, players, Deck(), 1, 1, None
      )

      val (next, _) = TrickState(1).run(control, state)

      next shouldBe Some(TrickState(2))
    }

    "move to ScoreState when hands are empty" in {
      val control = new GameControl(new MockView)

      val players = List(
        Player(0, List(NormalCard(CardColor.Red, 5))),
        Player(1, List(NormalCard(CardColor.Red, 7))),
        Player(2, List(NormalCard(CardColor.Red, 9)))
      )

      val state = GameState(
        3, players, Deck(), 1, 1, None
      )

      val (next, _) = TrickState(1).run(control, state)

      next shouldBe Some(ScoreState)
    }
  }

  // ============================================================
  // SCORE STATE
  // ============================================================
  "ScoreState" should {

    "score round and move to PrepareRoundState" in {
      val control = new GameControl(new MockView)

      val players = List(
        Player(0, Nil, tricks = 1, predictedTricks = 1),
        Player(1, Nil),
        Player(2, Nil)
      )

      val state = GameState(
        3, players, Deck(), 1, 1, None
      )

      val (next, s2) = ScoreState.run(control, state)

      next shouldBe Some(PrepareRoundState)
      s2.players.head.totalPoints shouldBe 30
    }
  }

  // ============================================================
  // FINISH STATE
  // ============================================================
  "FinishState" should {

    "determine winner and end game" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        3,
        List(
          Player(0, Nil, totalPoints = 10),
          Player(1, Nil, totalPoints = 50),
          Player(2, Nil, totalPoints = 20)
        ),
        Deck(), 1, 1, None
      )

      val (next, _) = FinishState.run(control, state)

      next shouldBe None
    }
  }
}
