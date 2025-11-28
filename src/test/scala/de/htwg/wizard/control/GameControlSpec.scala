package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameControlSpec extends AnyWordSpec with Matchers {

  // -------------------------------------------------------
  // MockView
  // -------------------------------------------------------
  class MockView(
                  playerAmounts: List[Int] = List(3),
                  predictions: List[Int] = List.fill(20)(0),
                  cardIndexes: List[Int] = List.fill(50)(0)
                ) extends GameView {

    private val paIter = playerAmounts.iterator
    private val predIter = predictions.iterator
    private val indexIter = cardIndexes.iterator

    var askPlayerAmountCalled = 0
    var readPlayerAmountCalled = 0
    var askHowManyCalled = 0
    var readPositiveCalled = 0
    var askCardCalled = 0
    var readIndexCalled = 0
    var showWinnerCalled = 0
    var showRoundEvalCalled = 0
    var showGameWinnerCalled = 0
    var lastError: String = ""

    override def askPlayerAmount(): Unit =
      askPlayerAmountCalled += 1

    override def readPlayerAmount(): Int = {
      readPlayerAmountCalled += 1
      paIter.next()
    }

    override def askHowManyTricks(p: Player): Unit =
      askHowManyCalled += 1

    override def readPositiveInt(): Int = {
      readPositiveCalled += 1
      predIter.next()
    }

    override def askPlayerCard(p: Player): Unit =
      askCardCalled += 1

    override def readIndex(p: Player): Int = {
      readIndexCalled += 1
      indexIter.next()
    }

    override def showTrickWinner(player: Player, card: Card): Unit =
      showWinnerCalled += 1

    override def showRoundEvaluation(round: Int, players: List[Player]): Unit =
      showRoundEvalCalled += 1

    override def showGameWinner(player: Player): Unit =
      showGameWinnerCalled += 1

    override def showError(message: String): Unit =
      lastError = message

    override def update(): Unit = ()
  }

  // -------------------------------------------------------
  // Deterministisches Deck
  // -------------------------------------------------------
  class TestDeck(cards: List[Card]) extends Deck(cards) {
    override def shuffle(): Deck = this
    override def deal(n: Int): (List[Card], Deck) =
      (cards.take(n), TestDeck(cards.drop(n)))
  }

  def fixedDeck(): Deck =
    TestDeck(
      List(
        NormalCard(CardColor.Blue, 1),
        NormalCard(CardColor.Green, 2),
        NormalCard(CardColor.Yellow, 3),
        NormalCard(CardColor.Red, 4),
        WizardCard(CardColor.Red),
        JokerCard(CardColor.Blue)
      )
    )

  // -------------------------------------------------------
  // TESTS
  // -------------------------------------------------------
  "GameControl" should {

    "initGame creates correct number of players" in {
      val view = new MockView(playerAmounts = List(3))
      val control = new GameControl(view)

      val m = control.getClass.getDeclaredMethod("initGame")
      m.setAccessible(true)

      val gs = m.invoke(control).asInstanceOf[GameState]

      gs.amountOfPlayers shouldBe 3
      gs.players.size shouldBe 3
      gs.players.forall(_.hand.isEmpty) shouldBe true
      view.askPlayerAmountCalled shouldBe 1
      view.readPlayerAmountCalled shouldBe 1
    }

    "prepareNextRound increments round and deals correct number of cards" in {
      val view = new MockView()
      val control = new GameControl(view)

      val gs0 = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = fixedDeck(),
        currentRound = 0,
        totalRounds = 5,
        currentTrump = None
      )

      val m = control.getClass.getDeclaredMethod("prepareNextRound", classOf[GameState])
      m.setAccessible(true)

      val gs = m.invoke(control, gs0).asInstanceOf[GameState]

      gs.currentRound shouldBe 1
      gs.players.foreach(_.hand.size shouldBe 1)
    }

    "calculateRoundPoints works for correct and wrong predictions" in {
      val control = new GameControl(new MockView())

      val m = control.getClass.getDeclaredMethod("calculateRoundPoints", classOf[Player])
      m.setAccessible(true)

      m.invoke(control, Player(0, tricks = 2, predictedTricks = 2))
        .asInstanceOf[Int] shouldBe 40

      m.invoke(control, Player(1, tricks = 1, predictedTricks = 3))
        .asInstanceOf[Int] shouldBe (10 - 20)
    }

    "playOneTrick plays cards and produces a winner" in {
      val view = new MockView(cardIndexes = List(0, 0, 0))
      val control = new GameControl(view)

      val players = List(
        Player(0, hand = List(NormalCard(CardColor.Red, 5))),
        Player(1, hand = List(NormalCard(CardColor.Blue, 2))),
        Player(2, hand = List(NormalCard(CardColor.Green, 3)))
      )

      val state = GameState(
        amountOfPlayers = 3,
        players = players,
        deck = fixedDeck(),
        currentRound = 1,
        totalRounds = 10,
        currentTrump = Some(CardColor.Red)
      )

      val m = control.getClass.getDeclaredMethod("playOneTrick", classOf[Int], classOf[GameState])
      m.setAccessible(true)

      val gs = m.invoke(control, Integer.valueOf(1), state).asInstanceOf[GameState]

      gs.currentTrick shouldBe None
      gs.players.exists(_.tricks == 1) shouldBe true
      view.askCardCalled shouldBe 3
      view.readIndexCalled shouldBe 3
      view.showWinnerCalled shouldBe 1
    }

    "playOneTrick shows error if any player has no cards" in {
      val view = new MockView()
      val control = new GameControl(view)

      val state = GameState(
        amountOfPlayers = 1,
        players = List(Player(0, hand = List())),
        deck = fixedDeck(),
        currentRound = 1,
        totalRounds = 10,
        currentTrump = None
      )

      val m = control.getClass.getDeclaredMethod("playOneTrick", classOf[Int], classOf[GameState])
      m.setAccessible(true)

      val result = m.invoke(control, Integer.valueOf(1), state).asInstanceOf[GameState]

      view.lastError shouldBe "No active stitch!"
      result shouldBe state
    }

  }
}
