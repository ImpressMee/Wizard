package de.htwg.wizard.control

import de.htwg.wizard.control.strategy.TrickStrategy
import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameControlSpec extends AnyWordSpec with Matchers {

  // -------------------------------------------------------------------------
  // Mock View
  // -------------------------------------------------------------------------

  class MockView(
                  playerAmounts: List[Int] = List(3),
                  predictions: List[Int] = List.fill(20)(0),
                  indexes: List[Int] = List.fill(50)(0)
                ) extends GameView {

    private val pa = playerAmounts.iterator
    private val pr = predictions.iterator
    private val ix = indexes.iterator

    var askPlayerAmountCalled = 0
    var readPlayerAmountCalled = 0
    var askHowManyCalled = 0
    var readPositiveCalled = 0
    var askCardCalled = 0
    var readIndexCalled = 0
    var showWinnerCalled = 0
    var showRoundInfoCalled = 0
    var showGameWinnerCalled = 0
    var lastError: String = ""

    override def askPlayerAmount(): Unit =
      askPlayerAmountCalled += 1

    override def readPlayerAmount(): Int =
      readPlayerAmountCalled += 1
      pa.next()

    override def askHowManyTricks(p: Player): Unit =
      askHowManyCalled += 1

    override def readPositiveInt(): Int =
      readPositiveCalled += 1
      pr.next()

    override def askPlayerCard(p: Player): Unit =
      askCardCalled += 1

    override def readIndex(p: Player): Int =
      readIndexCalled += 1
      ix.next()

    override def showTrickWinner(player: Player, card: Card): Unit =
      showWinnerCalled += 1

    override def showRoundInfo(r: Int, trump: Option[CardColor], players: Int): Unit =
      showRoundInfoCalled += 1

    override def showGameWinner(player: Player): Unit =
      showGameWinnerCalled += 1

    override def showError(msg: String): Unit =
      lastError = msg

    override def update(): Unit = ()
  }

  // -------------------------------------------------------------------------
  // Deterministic Deck for testing
  // -------------------------------------------------------------------------

  class TestDeck(cards: List[Card]) extends Deck(cards) {
    override def shuffle(): Deck = this
    override def deal(n: Int): (List[Card], Deck) =
      (cards.take(n), TestDeck(cards.drop(n)))
  }

  def fixedDeck(): Deck =
    TestDeck(
      List(
        NormalCard(CardColor.Red, 5),
        NormalCard(CardColor.Blue, 1),
        NormalCard(CardColor.Yellow, 9),
        NormalCard(CardColor.Green, 3),
        WizardCard(CardColor.Red),
        JokerCard(CardColor.Blue)
      )
    )

  // Reflection Helper
  def privateMethod(ctrl: GameControl, name: String, args: Class[?]*): java.lang.reflect.Method =
    val m = ctrl.getClass.getDeclaredMethod(name, args*)
    m.setAccessible(true)
    m

  // -------------------------------------------------------------------------
  // INIT GAME
  // -------------------------------------------------------------------------

  "initGame" should {

    "create correct number of players and shuffle deck" in {
      val view = new MockView(playerAmounts = List(3))
      val ctrl = new GameControl(view)

      val m = privateMethod(ctrl, "initGame")
      val gs = m.invoke(ctrl).asInstanceOf[GameState]

      gs.amountOfPlayers shouldBe 3
      gs.players.size shouldBe 3
      view.askPlayerAmountCalled shouldBe 1
      view.readPlayerAmountCalled shouldBe 1
    }

    "retry on invalid number" in {
      val view = new MockView(playerAmounts = List(99, 3))
      val ctrl = new GameControl(view)

      val m = privateMethod(ctrl, "initGame")
      m.invoke(ctrl)

      view.readPlayerAmountCalled shouldBe 2
    }

    "retry on NumberFormatException" in {
      val view = new MockView(playerAmounts = List(3)) {
        var fail = true
        override def readPlayerAmount(): Int =
          readPlayerAmountCalled += 1
          if fail then
            fail = false
            throw new NumberFormatException()
          else super.readPlayerAmount()
      }
      val ctrl = new GameControl(view)
      val m = privateMethod(ctrl, "initGame")
      m.invoke(ctrl)

      view.readPlayerAmountCalled should be >= 2
      view.askPlayerAmountCalled should be >= 2
    }
  }

  // -------------------------------------------------------------------------
  // PREPARE NEXT ROUND
  // -------------------------------------------------------------------------

  "prepareNextRound" should {

    "do nothing if currentRound >= totalRounds" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val gs = GameState(3, Nil, fixedDeck(), 3, 3, None)
      val m = privateMethod(ctrl, "prepareNextRound", classOf[GameState])

      m.invoke(ctrl, gs).asInstanceOf[GameState] shouldBe gs
    }

    "increment round, deal cards and pick trump" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val gs = GameState(
        3,
        List(Player(0), Player(1), Player(2)),
        fixedDeck(),
        0,
        5,
        None
      )

      val m = privateMethod(ctrl, "prepareNextRound", classOf[GameState])
      val result = m.invoke(ctrl, gs).asInstanceOf[GameState]

      result.currentRound shouldBe 1
      result.players.foreach(_.hand.size shouldBe 1)
      view.showRoundInfoCalled shouldBe 1
    }

    "call chooseTrump when first card is wizard" in {
      var called = 0

      val view = new MockView() {
        override def chooseTrump(): CardColor =
          called += 1
          CardColor.Green
      }
      val ctrl = new GameControl(view)

      val deck = new TestDeck(List(
        WizardCard(CardColor.Blue),
        NormalCard(CardColor.Red, 7)
      ))

      val gs = GameState(3, List(Player(0), Player(1), Player(2)), deck, 0, 5, None)

      val m = privateMethod(ctrl, "prepareNextRound", classOf[GameState])
      m.invoke(ctrl, gs)

      called shouldBe 1
    }

    "set trump = None when first card is joker" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val deck = new TestDeck(List(
        JokerCard(CardColor.Yellow),
        NormalCard(CardColor.Green, 3)
      ))

      val gs = GameState(3, List(Player(0), Player(1), Player(2)), deck, 0, 5, None)

      val m = privateMethod(ctrl, "prepareNextRound", classOf[GameState])
      val res = m.invoke(ctrl, gs).asInstanceOf[GameState]

      res.currentTrump shouldBe None
    }
  }

  // -------------------------------------------------------------------------
  // PREDICT TRICKS
  // -------------------------------------------------------------------------

  "predictTricks" should {

    "ask and store predictions" in {
      val view = new MockView(predictions = List(2, 1, 3))
      val ctrl = new GameControl(view)
      val gs = GameState(3, List(Player(0), Player(1), Player(2)), fixedDeck(), 1, 5, None)

      val m = privateMethod(ctrl, "predictTricks", classOf[GameState])
      val result = m.invoke(ctrl, gs).asInstanceOf[GameState]

      result.players.map(_.predictedTricks) shouldBe List(2, 1, 3)
      view.askHowManyCalled shouldBe 3
      view.readPositiveCalled shouldBe 3
    }
  }

  // -------------------------------------------------------------------------
  // PLAY ONE TRICK
  // -------------------------------------------------------------------------

  "playOneTrick" should {

    "abort if any player has empty hand" in {
      val view = new MockView()
      val ctrl = new GameControl(view)
      val gs = GameState(1, List(Player(0, hand = Nil)), fixedDeck(), 1, 5, None)

      val m = privateMethod(ctrl, "playOneTrick", classOf[Int], classOf[GameState])
      val result = m.invoke(ctrl, Integer.valueOf(1), gs).asInstanceOf[GameState]

      view.lastError shouldBe "No active stitch!"
      result shouldBe gs
    }

    "abort on illegal index" in {
      val view = new MockView(indexes = List(99))
      val ctrl = new GameControl(view)

      val gs = GameState(
        1,
        List(Player(0, hand = List(NormalCard(CardColor.Red, 5)))),
        fixedDeck(),
        1,
        5,
        None
      )

      val m = privateMethod(ctrl, "playOneTrick", classOf[Int], classOf[GameState])
      m.invoke(ctrl, Integer.valueOf(1), gs)

      view.lastError shouldBe "Invalid index!"
    }

    "play cards and select winner" in {
      val view = new MockView(indexes = List(0, 0, 0))
      val strategy = new TrickStrategy {
        override def winner(t: Trick, trump: Option[CardColor]): (Int, Card) =
          (1, NormalCard(CardColor.Blue, 1))
      }

      val ctrl = new GameControl(view, strategy)

      val gs = GameState(
        3,
        List(
          Player(0, hand = List(NormalCard(CardColor.Red, 5))),
          Player(1, hand = List(NormalCard(CardColor.Blue, 1))),
          Player(2, hand = List(NormalCard(CardColor.Green, 4)))
        ),
        fixedDeck(),
        1,
        5,
        Some(CardColor.Red)
      )

      val m = privateMethod(ctrl, "playOneTrick", classOf[Int], classOf[GameState])
      val result = m.invoke(ctrl, Integer.valueOf(1), gs).asInstanceOf[GameState]

      result.players(1).tricks shouldBe 1
      view.askCardCalled shouldBe 3
      view.readIndexCalled shouldBe 3
      view.showWinnerCalled shouldBe 1
    }
  }

  // -------------------------------------------------------------------------
  // SCORE ROUND
  // -------------------------------------------------------------------------

  "scoreRound" should {
    "calculate and reset points" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val players =
        List(
          Player(0, tricks = 2, predictedTricks = 2),
          Player(1, tricks = 1, predictedTricks = 3)
        )

      val gs = GameState(2, players, fixedDeck(), 1, 5, None)

      val m = privateMethod(ctrl, "scoreRound", classOf[GameState])
      val result = m.invoke(ctrl, gs).asInstanceOf[GameState]

      result.players.map(_.totalPoints) shouldBe List(40, -10)
      result.players.forall(p => p.tricks == 0 && p.predictedTricks == 0) shouldBe true
    }
  }

  // -------------------------------------------------------------------------
  // FINISH GAME
  // -------------------------------------------------------------------------

  "finishGame" should {

    "show winner" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val players =
        List(
          Player(0, totalPoints = 10),
          Player(1, totalPoints = 30)
        )

      val gs = GameState(2, players, fixedDeck(), 5, 5, None)

      val m = privateMethod(ctrl, "finishGame", classOf[GameState])
      m.invoke(ctrl, gs)

      view.showGameWinnerCalled shouldBe 1
    }

    "handle ties (first in list wins)" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val players =
        List(
          Player(0, totalPoints = 50),
          Player(1, totalPoints = 50)
        )

      val gs = GameState(2, players, fixedDeck(), 5, 5, None)

      val m = privateMethod(ctrl, "finishGame", classOf[GameState])
      m.invoke(ctrl, gs)

      view.showGameWinnerCalled shouldBe 1
    }
  }

  // -------------------------------------------------------------------------
  // UNDO
  // -------------------------------------------------------------------------

  "undo" should {

    "return same state if history is empty" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val gs = GameState(1, Nil, fixedDeck(), 0, 1, None)

      ctrl.undo(gs) shouldBe gs
    }

    "restore previous state if history has one entry" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val gs1 = GameState(1, List(Player(0)), fixedDeck(), 0, 1, None)
      val gs2 = gs1.copy(currentRound = 5)

      val save = privateMethod(ctrl, "saveState", classOf[GameState])
      save.invoke(ctrl, gs1)

      ctrl.undo(gs2) shouldBe gs1
    }

    "restore oldest state if history has multiple entries" in {
      val view = new MockView()
      val ctrl = new GameControl(view)

      val gs1 = GameState(1, Nil, fixedDeck(), 1, 2, None)
      val gs2 = GameState(1, Nil, fixedDeck(), 2, 2, None)
      val gs3 = GameState(1, Nil, fixedDeck(), 3, 2, None)

      val save = privateMethod(ctrl, "saveState", classOf[GameState])
      save.invoke(ctrl, gs1)
      save.invoke(ctrl, gs2)

      ctrl.undo(gs3) shouldBe gs2
      ctrl.undo(gs2) shouldBe gs1
    }
  }

  // -------------------------------------------------------------------------
  // RUN GAME
  // -------------------------------------------------------------------------

  "runGame" should {

    "stop when a phase returns null" in {

      val view = new MockView()

      class DummyPhase extends GameStatePhase {
        var count = 0
        override def run(c: GameControl, gs: GameState) =
          count += 1
          if count >= 2 then (null, gs) else (this, gs)
      }

      val ctrl = new GameControl(view) {
        override def runGame(): Unit =
          var phase: GameStatePhase = new DummyPhase
          var state: GameState = GameState(0, Nil, Deck(Nil), 0, 0, None)
          while phase != null do
            val (np, ns) = phase.run(this, state)
            phase = np
            state = ns
      }

      ctrl.runGame()
      succeed
    }
  }
}
