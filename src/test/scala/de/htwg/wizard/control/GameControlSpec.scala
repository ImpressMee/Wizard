package de.htwg.wizard.control

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import de.htwg.wizard.view.GameView
import de.htwg.wizard.control.strategy.StandardTrickStrategy

class GameControlSpec extends AnyWordSpec with Matchers {

  // ============================================================
  // MockView (deterministisch, retry-sicher)
  // ============================================================
  class MockView extends GameView {

    private var playerAmounts = List(1, 3)

    override def readPlayerAmount(): Int =
      val h = playerAmounts.head
      playerAmounts = playerAmounts.tail
      h

    override def readPositiveInt(): Int = 1
    override def readIndex(p: Player): Int = 0
    override def chooseTrump(): CardColor = CardColor.Red

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
  // Tests
  // ============================================================
  "GameControl" should {

    "initialize game with retry on invalid player amount" in {
      val control = new GameControl(new MockView)

      val state = control.doInitGame()

      state.amountOfPlayers shouldBe 3
      state.players.size shouldBe 3
      state.currentRound shouldBe 0
    }

    "prepare next round and deal cards" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = Deck(),
        currentRound = 0,
        totalRounds = 1,
        currentTrump = None
      )

      val next = control.doPrepareNextRound(state)

      next.currentRound shouldBe 1
      next.players.forall(_.hand.size == 1) shouldBe true
    }

    "not prepare a round when currentRound >= totalRounds" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val next = control.doPrepareNextRound(state)

      next shouldBe state
    }

    "predict tricks for all players" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val next = control.doPredictTricks(state)

      next.players.forall(_.predictedTricks == 1) shouldBe true
    }

    "reject illegal color follow and keep state unchanged" in {
      val view = new MockView {
        override def readIndex(p: Player): Int =
          if p.id == 1 then 1 else 0
      }

      val control = new GameControl(view, StandardTrickStrategy())

      val players = List(
        Player(0, List(NormalCard(CardColor.Red, 5))),
        Player(1, List(NormalCard(CardColor.Red, 3), NormalCard(CardColor.Blue, 7))),
        Player(2, List(NormalCard(CardColor.Red, 9)))
      )

      val state = GameState(
        amountOfPlayers = 3,
        players = players,
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val result = control.doPlayOneTrick(1, state)

      result.players.map(_.hand) shouldBe players.map(_.hand)
    }

    "play a valid trick and increment winner tricks" in {
      val control = new GameControl(new MockView, StandardTrickStrategy())

      val players = List(
        Player(0, List(NormalCard(CardColor.Red, 5))),
        Player(1, List(NormalCard(CardColor.Red, 7))),
        Player(2, List(NormalCard(CardColor.Red, 9)))
      )

      val state = GameState(
        amountOfPlayers = 3,
        players = players,
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val result = control.doPlayOneTrick(1, state)

      result.players.exists(_.tricks == 1) shouldBe true
      result.players.forall(_.hand.isEmpty) shouldBe true
    }

    "score round correctly and reset players" in {
      val control = new GameControl(new MockView)

      val players = List(
        Player(0, Nil, tricks = 1, predictedTricks = 1),
        Player(1, Nil),
        Player(2, Nil)
      )

      val state = GameState(
        amountOfPlayers = 3,
        players = players,
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      val next = control.doScoreRound(state)

      next.players.head.totalPoints shouldBe 30
      next.players.forall(_.tricks == 0) shouldBe true
      next.players.forall(_.predictedTricks == 0) shouldBe true
    }

    "determine the game winner" in {
      val control = new GameControl(new MockView)

      val state = GameState(
        amountOfPlayers = 3,
        players = List(
          Player(0, Nil, totalPoints = 10),
          Player(1, Nil, totalPoints = 50),
          Player(2, Nil, totalPoints = 20)
        ),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 1,
        currentTrump = None
      )

      noException shouldBe thrownBy {
        control.doDetermineWinner(state)
      }
    }

    "undo restores previous state after prepare round" in {
      val control = new GameControl(new MockView)

      val s1 = GameState(3, List(Player(0), Player(1), Player(2)), Deck(), 0, 1, None)
      val s2 = control.doPrepareNextRound(s1)

      val restored = control.undo(s2)

      restored.currentRound shouldBe 0
    }
  }
}
