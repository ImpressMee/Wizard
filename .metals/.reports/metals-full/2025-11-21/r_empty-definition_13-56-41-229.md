error id: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/test/scala/de/htwg/wizard/GameView.scala:`<none>`.
file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/test/scala/de/htwg/wizard/GameView.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -de/htwg/wizard/model/AnyWordSpec#
	 -org/scalatest/wordspec/AnyWordSpec#
	 -AnyWordSpec#
	 -scala/Predef.AnyWordSpec#
offset: 242
uri: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/test/scala/de/htwg/wizard/GameView.scala
text:

```scala
package de.htwg.wizard.view

import de.htwg.wizard.model.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import java.io.{ByteArrayOutputStream, PrintStream}

class GameViewTest extends AnyWordS

@@ pec
with Matchers {

  // Hilfsfunktion zum Abfangen von Konsolenausgabe
  def captureOutput(block: => Unit): String = {
    val outCapture = new ByteArrayOutputStream()
    Console.withOut(new PrintStream(outCapture)) {
      block
    }
    outCapture.toString
  }

  val view = new GameView
  val player = Player(0, List(Card(CardColor.Red, 1), Card(CardColor.Blue, 2)))
  val players = List(player, Player(1, List(Card(CardColor.Green, 3))))

  "GameView" should {

    "print askPlayerAmount prompt" in {
      val output = captureOutput {
        view.askPlayerAmount()
      }
      output should include("How many Players are playing? (3-6):")
    }

    "print round info" in {
      val output = captureOutput {
        view.showRoundInfo(2, CardColor.Green, 4)
      }
      output should include("round: 2")
      output should include("Trump is: Green")
      output should include("There are 4 players.")
    }

    "print player cards" in {
      val output = captureOutput {
        view.showPlayerCards(players)
      }
      output should include("Player 0")
      output should include("Player 1")
      output should include("Red")
      output should include("Green")
    }

    "print askNewStitches prompt" in {
      val output = captureOutput {
        view.askHowManyTricks(player)
      }
      output should include("How many Stitches will you make player0")
    }

    "print stitch start" in {
      val output = captureOutput {
        view.showTrickStart()
      }
      output should include("Stitch start")
    }

    "print askPlayerCard prompt" in {
      val output = captureOutput {
        view.askPlayerCard(player)
      }
      output should include("Which card do you wanna play Player 0?")
      output should include("Red")
      output should include("Blue")
    }

    "print stitch winner" in {
      val output = captureOutput {
        view.showTrickWinner(player, Card(CardColor.Yellow, 3))
      }
      output should include("Player 0 won this stitch with")
      output should include("Yellow")
    }

    "print round evaluation" in {
      val evaluatedPlayers = List(
        player.copy(predictedStitches = 2, stitches = 2, totalPoints = 40)
      )
      val output = captureOutput {
        view.showRoundEvaluation(1, evaluatedPlayers)
      }
      output should include("Round 1 -- Evaluation")
      output should include("Player 0")
      output should include("40 points")
    }

    "print game winner" in {
      val output = captureOutput {
        view.showGameWinner(player.copy(totalPoints = 99))
      }
      output should include("And the winner is Player0 mit 99")
    }

    "print error message in red" in {
      val output = captureOutput {
        view.showError("Oops!")
      }
      output should include("Oops!")
      output should include(Console.RED)
    }

    "react to update call" in {
      val output = captureOutput {
        view.update()
      }
      output should include("Do sumthin")
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.