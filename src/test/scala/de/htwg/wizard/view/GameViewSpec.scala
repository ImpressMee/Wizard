package de.htwg.wizard.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.model.*
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.Console
import scala.util.Success

class GameViewSpec extends AnyWordSpec with Matchers {

  private def withOutput(body: => Unit): String = {
    val out = new ByteArrayOutputStream()
    Console.withOut(out) { body }
    out.toString
  }

  private def withInput(input: String)(body: => Any): Any =
    Console.withIn(new ByteArrayInputStream(input.getBytes)) { body }

  "GameView" should {

    "write cards correctly" in {
      val view = new GameView
      view.writeOneCard(NormalCard(CardColor.Red, 5)) shouldBe "Red 5"
      view.writeOneCard(WizardCard(CardColor.Blue)) shouldBe "Blue WIZARD"
      view.writeOneCard(JokerCard(CardColor.Green)) shouldBe "Green JOKER"
    }

    "ask player amount (print only)" in {
      val view = new GameView
      val out = withOutput { view.askPlayerAmount() }
      out should include ("Game Start")
    }

    "read valid player amount" in {
      val view = new GameView
      val result = withInput("4\n") { view.readPlayerAmount() }
      result shouldBe Success(4)
    }

    "choose trump with retry" in {
      val view = new GameView
      val result = withInput("9\n3\n") { view.chooseTrump() }
      result shouldBe CardColor.Blue
    }

    "show round info with and without trump" in {
      val view = new GameView
      withOutput {
        view.showRoundInfo(1, Some(CardColor.Red), 3)
        view.showRoundInfo(2, None, 4)
      }
    }

    "show player cards" in {
      val view = new GameView
      val player = Player(
        id = 1,
        hand = List(
          NormalCard(CardColor.Red, 3),
          WizardCard(CardColor.Blue),
          JokerCard(CardColor.Green)
        )
      )
      val out = withOutput { view.showPlayerCards(player) }
      out should include ("Player1")
    }

    "ask how many tricks" in {
      val view = new GameView
      withOutput { view.askHowManyTricks(Player(0)) }
    }

    "read positive int with retry" in {
      val view = new GameView
      val result = withInput("-1\nabc\n2\n") { view.readPositiveInt() }
      result shouldBe 2
    }

    "show trick start" in {
      val view = new GameView
      withOutput { view.showTrickStart(1) }
    }

    "ask player card" in {
      val view = new GameView
      val player = Player(0, List(NormalCard(CardColor.Red, 1)))
      withOutput { view.askPlayerCard(player) }
    }

    "read valid card index with retry" in {
      val view = new GameView
      val player = Player(0, List(
        NormalCard(CardColor.Red, 1),
        NormalCard(CardColor.Blue, 2)
      ))
      val result = withInput("0\n3\n2\n") { view.readIndex(player) }
      result shouldBe 1
    }

    "show trick winner" in {
      val view = new GameView
      withOutput { view.showTrickWinner(Player(1), NormalCard(CardColor.Yellow, 7)) }
    }

    "show round evaluation" in {
      val view = new GameView
      val players = List(
        Player(0, tricks = 1, predictedTricks = 1, totalPoints = 30),
        Player(1, tricks = 0, predictedTricks = 1, totalPoints = -10)
      )
      withOutput { view.showRoundEvaluation(1, players) }
    }

    "show game winner" in {
      val view = new GameView
      withOutput { view.showGameWinner(Player(0, totalPoints = 50)) }
    }

    "show error" in {
      val view = new GameView
      val out = withOutput { view.showError("error") }
      out should include ("error")
    }

    "update observer" in {
      val view = new GameView
      val out = withOutput { view.update() }
      out should include ("update display")
    }
  }
}
