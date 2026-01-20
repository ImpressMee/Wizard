package de.htwg.wizard.persistence

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.wizard.persistence.json.FileIOJson
import de.htwg.wizard.persistence.xml.FileIOXML
import de.htwg.wizard.model.modelComponent.GameState
import java.io.File

class FileIOSpec extends AnyWordSpec with Matchers {

  "FileIOJson" should {

    val file = new File("wizard.json")
    file.delete()

    val fio = new FileIOJson

    "report no save when file is missing" in {
      fio.hasSave shouldBe false
    }

    "save and load GameState" in {
      fio.save(GameState.empty)
      fio.hasSave shouldBe true

      val loaded = fio.load()
      loaded.amountOfPlayers shouldBe 0
    }
  }

  "FileIOXML" should {

    val file = new File("wizard.xml")
    file.delete()

    val fio = new FileIOXML

    "report no save when file is missing" in {
      fio.hasSave shouldBe false
    }

    "save and load GameState" in {
      fio.save(GameState.empty)
      fio.hasSave shouldBe true

      val loaded = fio.load()
      loaded.players shouldBe Nil
    }
  }
}
