error id: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/test/scala/de/htwg/wizard/DeckTest.scala:`<none>`.
file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/test/scala/de/htwg/wizard/DeckTest.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -deck/shuffle.
	 -deck/shuffle#
	 -deck/shuffle().
	 -scala/Predef.deck.shuffle.
	 -scala/Predef.deck.shuffle#
	 -scala/Predef.deck.shuffle().
offset: 448
uri: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/test/scala/de/htwg/wizard/DeckTest.scala
text:
```scala
package de.htwg.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DeckTest extends AnyWordSpec with Matchers {

  "A Deck" should {

    "deal cards correctly" in {
      val deck = new Deck()
      val dealt = deck.deal(3)
      dealt.length shouldBe 3
    }

    "shuffle without throwing" in {
      val deck = new Deck()
      noException should be thrownBy deck.shuff@@le()
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.