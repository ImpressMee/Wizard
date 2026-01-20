package de.htwg.wizard

import org.scalatest.Ignore
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scalafx.application.Platform

import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicBoolean


class MainSpec extends AnyWordSpec with Matchers {

  // ---------------------------------------------------------
  // JavaFX bootstrap (einmalig, nicht blockierend)
  // ---------------------------------------------------------
  private val fxStarted = new AtomicBoolean(false)

  private def initFx(): Unit =
    if (fxStarted.compareAndSet(false, true)) {
      Platform.startup(() => ())
    }

  private def runFx(block: => Unit): Unit = {
    initFx()
    val latch = new CountDownLatch(1)

    Platform.runLater {
      try {
        block
      } finally {
        latch.countDown()
      }
    }

    latch.await(5, TimeUnit.SECONDS) shouldBe true
  }

  // ---------------------------------------------------------
  // Tests
  // ---------------------------------------------------------
  "Main (application entry point)" should {

    "start without throwing exceptions" ignore runFx {
      noException shouldBe thrownBy {
        Main.start()
      }
    }

    "create a primary stage" in runFx {
      Main.start()
      Main.stage should not be null
    }

    "set application title correctly" in runFx {
      Main.start()
      Main.stage.title.value shouldBe "Wizard"
    }

    "register observers and show GUI without crashing" in runFx {
      noException shouldBe thrownBy {
        Main.start()
      }
    }
  }
}
