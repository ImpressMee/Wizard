package de.htwg.wizard.control

import de.htwg.wizard.model.*
import de.htwg.wizard.view.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameControlSpec extends AnyWordSpec with Matchers {

  // ------------------------------------------------------------
  // MockObserver (for testing of notifyObservers())
  // ------------------------------------------------------------
  class MockObserver extends Observer {
    var updates = 0
    override def update(): Unit = updates += 1
  }

  // ------------------------------------------------------------
  // MockView (ALL View-Methods overridden)
  // =>predictable inputs
  // ------------------------------------------------------------
  class MockView(
                  playerAmounts: List[Int] = List(3),
                  predictions: List[Int] = List.fill(10)(0),
                  cardIndexes: List[Int] = List.fill(30)(0)
                ) extends GameView {

    var msg: String = ""
    var askPlayerAmountCalled = 0
    var readPlayerAmountCalled = 0
    var showRoundInfoCalled = 0
    var askHowManyTricksCalled = 0
    var readPositiveIntCalled = 0
    var askPlayerCardCalled = 0
    var readIndexCalled = 0
    var showTrickWinnerCalled = 0
    var showRoundEvaluationCalled = 0
    var showGameWinnerCalled = 0

    private var paIter = playerAmounts.iterator
    private var predIter = predictions.iterator
    private var indexIter = cardIndexes.iterator

    override def askPlayerAmount(): Unit =
      askPlayerAmountCalled += 1

    override def readPlayerAmount(): Int =
      readPlayerAmountCalled += 1
      paIter.next()

    override def askHowManyTricks(p: Player): Unit =
      askHowManyTricksCalled += 1

    override def readPositiveInt(): Int =
      readPositiveIntCalled += 1
      predIter.next()

    override def askPlayerCard(p: Player): Unit =
      askPlayerCardCalled += 1

    override def readIndex(p: Player): Int =
      readIndexCalled += 1
      indexIter.next()

    override def showTrickWinner(player: Player, card: Card): Unit =
      showTrickWinnerCalled += 1

    override def showRoundEvaluation(round: Int, players: List[Player]): Unit =
      showRoundEvaluationCalled += 1

    override def showGameWinner(player: Player): Unit =
      showGameWinnerCalled += 1

    override def showError(message: String): Unit =
      msg = message

    override def update(): Unit = ()  // kein println
  }

  // ------------------------------------------------------------
  // deck for reproducible tests
  // ------------------------------------------------------------
  class TestDeck(cards: List[Card]) extends Deck(cards) {
    override def shuffle(): Deck = this
    override def deal(n: Int): (List[Card], Deck) =
      (cards.take(n), TestDeck(cards.drop(n)))
  }

  def fixedDeck(): Deck =
    TestDeck(
      List(
        Card(CardColor.Red, 1),
        Card(CardColor.Blue, 2),
        Card(CardColor.Green, 3),
        Card(CardColor.Yellow, 4),
        Card(CardColor.Red, 5),
        Card(CardColor.Blue, 6)
      )
    )

  "initGame handles NumberFormatException and retries" in {
    class ErrorMockView extends MockView(playerAmounts = List(3)) {
      var errorThrown = false

      override def readPlayerAmount(): Int =
        if !errorThrown then
          errorThrown = true
          throw new NumberFormatException()
        else
          3 // beim zweiten Mal gültig

      override def showError(msg: String): Unit =
        msg shouldBe "Invalid entry! Try again."
    }

    val view = new ErrorMockView
    val control = new GameControl(view)

    val method = control.getClass.getDeclaredMethod("initGame")
    method.setAccessible(true)
    val gs = method.invoke(control).asInstanceOf[GameState]

    gs.amountOfPlayers shouldBe 3
    view.errorThrown shouldBe true
  }

  "playOneTrick shows error when no active stitch exists" in {
    val view = new MockView()
    var errorMsg = ""

    // showError überschreiben
    val customView = new MockView() {
      override def showError(msg: String): Unit =
        errorMsg = msg
    }

    val control = new GameControl(customView)

    // Zustand erzeugen, in dem kein Trick aktiv ist
    val state = GameState(
      amountOfPlayers = 1,
      players = List(Player(0, hand = List())), // keine Karten -> finish-Trick führt zu None
      deck = fixedDeck(),
      currentRound = 1,
      totalRounds = 10,
      currentTrump = CardColor.Red,
      currentTrick = None
    )

    val method = control.getClass.getDeclaredMethod("playOneTrick", classOf[GameState])
    method.setAccessible(true)

    val result = method.invoke(control, state).asInstanceOf[GameState]

    errorMsg shouldBe "No active stitch!"
    result shouldBe state
  }



  // ------------------------------------------------------------
  // TESTS
  // ------------------------------------------------------------

  "GameControl" should {

    "initialize the game correctly" in {
      val view = new MockView(playerAmounts = List(3))
      val control = new GameControl(view)

      val state = control
        .getClass
        .getDeclaredMethod("initGame")
        .nn
      state.setAccessible(true)
      val gs = state.invoke(control).asInstanceOf[GameState]

      gs.amountOfPlayers shouldBe 3
      gs.players.size shouldBe 3
      gs.players.forall(_.hand.isEmpty) shouldBe true
      view.askPlayerAmountCalled shouldBe 1
      view.readPlayerAmountCalled shouldBe 1
    }

    "prepareNextRound distributes cards and increments round" in {
      val view = new MockView()
      val control = new GameControl(view)

      val initial = GameState(
        amountOfPlayers = 3,
        players = List(Player(0), Player(1), Player(2)),
        deck = fixedDeck(),
        currentRound = 0,
        totalRounds = 20,
        currentTrump = CardColor.Red
      )

      val method = control.getClass.getDeclaredMethod("prepareNextRound", classOf[GameState])
      method.setAccessible(true)
      val gs = method.invoke(control, initial).asInstanceOf[GameState]

      gs.currentRound shouldBe 1
      gs.players.foreach(_.hand.size shouldBe 1)
    }

    "calculateRoundPoints correctly" in {
      val view = new MockView()
      val control = new GameControl(view)

      val calc = control.getClass.getDeclaredMethod("calculateRoundPoints", classOf[Player])
      calc.setAccessible(true)

      val p1 = Player(1, tricks = 2, predictedTricks = 2)
      val p2 = Player(2, tricks = 1, predictedTricks = 3)

      calc.invoke(control, p1).asInstanceOf[Int] shouldBe 20 + 2 * 10
      calc.invoke(control, p2).asInstanceOf[Int] shouldBe (1 * 10 + (-10 * 2))
    }

    "playOneTrick updates players and ends with winner" in {
      val view = new MockView(predictions = List(), cardIndexes = List(0, 0, 0))
      val control = new GameControl(view)

      val players = List(
        Player(0, hand = List(Card(CardColor.Red, 5))),
        Player(1, hand = List(Card(CardColor.Blue, 2))),
        Player(2, hand = List(Card(CardColor.Green, 3)))
      )

      val state = GameState(
        amountOfPlayers = 3,
        players = players,
        deck = fixedDeck(),
        currentRound = 1,
        totalRounds = 20,
        currentTrump = CardColor.Red
      )

      val method = control.getClass.getDeclaredMethod("playOneTrick", classOf[GameState])
      method.setAccessible(true)
      val gs = method.invoke(control, state).asInstanceOf[GameState]

      gs.currentTrick shouldBe None
      gs.players.exists(_.tricks == 1) shouldBe true
      view.askPlayerCardCalled shouldBe 3
      view.readIndexCalled shouldBe 3
      view.showTrickWinnerCalled shouldBe 1
    }

    "whoWonStitch evaluates correct winner" in {
      val view = new MockView()
      val control = new GameControl(view)

      val stitch = Trick(
        Map(
          0 -> Card(CardColor.Green, 3),
          1 -> Card(CardColor.Red, 1),
          2 -> Card(CardColor.Green, 5)
        )
      )

      val method = control.getClass.getDeclaredMethod("whoWonStitch", classOf[Trick], classOf[CardColor])
      method.setAccessible(true)

      val (winner, card) =
        method.invoke(control, stitch, CardColor.Red).asInstanceOf[(Int, Card)]

      winner shouldBe 1
      card shouldBe Card(CardColor.Red, 1)
    }

    "runGame executes all rounds and ends with winner" in {
      val view = new MockView(
        playerAmounts = List(3),
        predictions = List.fill(20)(0),
        cardIndexes = List.fill(200)(0)
      )

      val control = new GameControl(view)

      noException shouldBe thrownBy {
        control.runGame()
      }

      view.showGameWinnerCalled shouldBe 1
      view.askPlayerAmountCalled shouldBe 1
    }

    "playOneTrick shows error when no active stitch exists" in {

      class ErrorView extends MockView() {
        var lastError = ""
        override def showError(msg: String): Unit =
          lastError = msg
      }

      val view = new ErrorView
      val control = new GameControl(view)

      val state = GameState(
        amountOfPlayers = 1,
        players = List(Player(0, hand = List())),
        deck = fixedDeck(),
        currentRound = 1,
        totalRounds = 10,
        currentTrump = CardColor.Red,
        currentTrick = None
      )

      val method = control.getClass.getDeclaredMethod("playOneTrick", classOf[GameState])
      method.setAccessible(true)

      val result = method.invoke(control, state).asInstanceOf[GameState]

      view.lastError shouldBe "No active stitch!"
      result shouldBe state
    }

    "playOneTrick should show error when currentTrick is None" in {

      class ErrorView extends MockView() {
        var lastError = ""

        override def showError(msg: String): Unit =
          lastError = msg
      }

      val view = new ErrorView
      val control = new GameControl(view)

      val state = GameState(
        amountOfPlayers = 1,
        players = List(Player(0, hand = List())),
        deck = Deck(),
        currentRound = 1,
        totalRounds = 5,
        currentTrump = CardColor.Red,
        currentTrick = None
      )

      val method = control.getClass.getDeclaredMethod("playOneTrick", classOf[GameState])
      method.setAccessible(true)

      val result = method.invoke(control, state).asInstanceOf[GameState]

      view.lastError shouldBe "No active stitch!"
      result shouldBe state
    }

    "whoWonStitch picks higher trump card when both are trump" in {
      val view = new MockView()
      val control = new GameControl(view)

      // Trumpf ist RED
      val trump = CardColor.Red

      // Zwei Trumpfkarten → zweite ist höher
      val stitch = Trick(
        Map(
          0 -> Card(CardColor.Red, 5), // bestCard: Wert 5
          1 -> Card(CardColor.Red, 8) // card: Wert 8 → gewinnt durch (card.value > bestCard.value)
        )
      )

      val method = control.getClass.getDeclaredMethod("whoWonStitch", classOf[Trick], classOf[CardColor])
      method.setAccessible(true)

      val (winner, card) =
        method.invoke(control, stitch, trump).asInstanceOf[(Int, Card)]

      winner shouldBe 1
      card shouldBe Card(CardColor.Red, 8)
    }


  }
}
