error id: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/main/scala/de/htwg/wizard/model/Card.scala:`<none>`.
file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/main/scala/de/htwg/wizard/model/Card.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -scala/util/Random.nextInt.
	 -scala/util/Random.nextInt#
	 -scala/util/Random.nextInt().
	 -Random.nextInt.
	 -Random.nextInt#
	 -Random.nextInt().
	 -scala/Predef.Random.nextInt.
	 -scala/Predef.Random.nextInt#
	 -scala/Predef.Random.nextInt().
offset: 223
uri: file:///C:/Users/Nikita/Desktop/Wizard_Repo/Wizard/src/main/scala/de/htwg/wizard/model/Card.scala
text:

```scala
package de.htwg.wizard.model

import scala.util.Random

enum CardColor:
  case Red, Green, Blue, Yellow

case class Card(color: wizard.CardColor, value: Int)

def trumpColor(): wizard.CardColor =
  CardColor.values(Random.nextIn @@ t(CardColor.values.length))

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.