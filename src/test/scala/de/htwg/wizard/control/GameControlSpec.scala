package de.htwg.wizard.control

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

    override def readPlayerAmount(): Int = {
      readPlayerAmountCalled += 1
      pa.next()
    }

    override def askHowManyTricks(p: Player): Unit =
      askHowManyCalled += 1

    override def readPositiveInt(): Int = {
      readPositiveCalled += 1
      pr.next()
    }

    override def askPlayerCard(p: Player): Unit =
      askCardCalled += 1

    override def readIndex(p: Player): Int = {
      readIndexCalled += 1
      ix.next()
    }

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
  // Deterministisches Deck für Tests
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

  // -------------------------------------------------------------------------
  // Hilfsmethoden für Reflection
  // -------------------------------------------------------------------------

  def privateMethod(control: GameControl, name: String, args: Class[?]*): java.lang.reflect.Method =
    val m = control.getClass.getDeclaredMethod(name, args*)
    m.setAccessible(true)
    m

  // -------------------------------------------------------------------------
  // TESTS FÜR INIT GAME
  // -------------------------------------------------------------------------

  "initGame" should {
    "create correct number of players and shuffle deck" in {
      val view = new MockView(playerAmounts = List(3))
      val ctrl = new GameControl(view)

      val m = privateMethod(ctrl, "initGame")
      val gs = m.invoke(ctrl).asInstanceOf[GameState]

      gs.amountOfPlayers shouldBe 3
      gs.players.size shouldBe 3
      gs.players.forall(_.hand.isEmpty) shouldBe true
      view.askPlayerAmountCalled shouldBe 1
      view.readPlayerAmountCalled shouldBe 1
    }

    "retry on invalid number" in {
      val view = new MockView(playerAmounts = List(99, 3)) // first invalid
      val ctrl = new GameControl(view)

      val m = privateMethod(ctrl, "initGame")
      m.invoke(ctrl).asInstanceOf[GameState]

      view.readPlayerAmountCalled shouldBe 2
    }
  }

  // -------------------------------------------------------------------------
  // TESTS FÜR PREPARE ROUND
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
  }

  // -------------------------------------------------------------------------
  // TESTS FÜR PREDICT TRICKS
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
  // TESTS FÜR PLAY ONE TRICK
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
  }

  // -------------------------------------------------------------------------
  // TESTS FÜR SCORE ROUND
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

      // Points updated?
      result.players.map(_.totalPoints) shouldBe List(40, -10)

      // Predictions + tricks reset?
      result.players.forall(p => p.tricks == 0 && p.predictedTricks == 0) shouldBe true
    }
  }

  // -------------------------------------------------------------------------
  // TESTS FÜR FINISH GAME
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
      m.invoke(ctrl, gs).asInstanceOf[Unit]

      view.showGameWinnerCalled shouldBe 1
    }
  }
}
