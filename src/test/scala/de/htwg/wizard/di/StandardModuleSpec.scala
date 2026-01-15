package de.htwg.wizard.di

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import com.google.inject.Guice
import de.htwg.wizard.control.GamePort
import de.htwg.wizard.control.controlComponent.strategy.TrickStrategy
import de.htwg.wizard.model.ModelInterface
import de.htwg.wizard.persistence.FileIO

class StandardModuleSpec extends AnyWordSpec with Matchers {

  "StandardModule" should {

    "provide a GamePort implementation" in {
      val injector = Guice.createInjector(new StandardModule)

      val gamePort = injector.getInstance(classOf[GamePort])

      gamePort should not be null
    }

    "bind FileIO to an implementation" in {
      val injector = Guice.createInjector(new StandardModule)

      val fileIO = injector.getInstance(classOf[FileIO])

      fileIO should not be null
    }

    "bind TrickStrategy to an implementation" in {
      val injector = Guice.createInjector(new StandardModule)

      val strategy = injector.getInstance(classOf[TrickStrategy])

      strategy should not be null
    }

    "bind ModelInterface to an implementation" in {
      val injector = Guice.createInjector(new StandardModule)

      val model = injector.getInstance(classOf[ModelInterface])

      model should not be null
    }
  }
}
