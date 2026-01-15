package de.htwg.wizard.persistence.json

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.*
import java.io.File

class FileIOJsonSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // Helper
  // ---------------------------------------------------------
  private val file = new File("wizard.json")

  private def cleanUp(): Unit =
    if (file.exists()) file.delete()

  // ---------------------------------------------------------
  // Test data
  // ---------------------------------------------------------
  val state: GameState =
    GameState(
      amountOfPlayers = 2,
      players = List(
        Player(
          id = 0,
          hand = List(
            NormalCard(CardColor.Red, 3),
            WizardCard(CardColor.Blue)
          ),
          tricks = 1,
          predictedTricks = 1,
          totalPoints = 10
        ),
        Player(
          id = 1,
          hand = List(
            JokerCard(CardColor.Green)
          ),
          tricks = 0,
          predictedTricks = 1,
          totalPoints = 0
        )
      ),
      deck = Deck(),
      currentRound = 2,
      totalRounds = 5,
      currentTrump = Some(CardColor.Red),
      currentTrick = Some(
        Trick(Map(0 -> NormalCard(CardColor.Red, 3)))
      ),
      completedTricks = 1
    )

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "FileIOJson" should {

    "report no save if no file exists" in {
      cleanUp()
      val io = new FileIOJson

      io.hasSave shouldBe false
    }

    "load the same GameState that was saved" in {
      cleanUp()
      val io = new FileIOJson

      io.save(state)
      val loaded = io.load()

      loaded shouldBe state
    }

    "return GameState.empty if no save file exists" in {
      cleanUp()
      val io = new FileIOJson

      io.load() shouldBe GameState.empty
    }
  }
}
