package de.htwg.wizard.persistence.xml

import de.htwg.wizard.persistence.FileIO
import de.htwg.wizard.model.modelComponent.*
import de.htwg.wizard.model.*
import scala.xml.*
import java.io.File

class FileIOXML extends FileIO {

  override def hasSave: Boolean =
    file.exists()

  private val file = new File("wizard.xml")

  // -------------------------
  // SAVE
  // -------------------------
  override def save(state: GameState): Unit = {

    def cardToXml(card: Card): Elem = card match
      case NormalCard(color, value) =>
          <card type="normal" color={color.toString} value={value.toString}/>
      case WizardCard(color) =>
          <card type="wizard" color={color.toString}/>
      case JokerCard(color) =>
          <card type="joker" color={color.toString}/>

    val playersXml =
      state.players.map { p =>
        <player>
          <id>{p.id}</id>
          <points>{p.totalPoints}</points>
          <predicted>{p.predictedTricks}</predicted>
          <won>{p.tricks}</won>
          <hand>{p.hand.map(cardToXml)}</hand>
        </player>
      }

    val deckXml =
      <deck>{state.deck.cards.map(cardToXml)}</deck>

    val trickXml =
      state.currentTrick.map { trick =>
        <trick>{
          trick.played.map { case (pid, card) =>
            <move player={pid.toString}>{cardToXml(card)}</move>
          }
          }</trick>
      }

    val xml =
      <game>
        <meta>
          <amountOfPlayers>{state.amountOfPlayers}</amountOfPlayers>
          <currentRound>{state.currentRound}</currentRound>
          <totalRounds>{state.totalRounds}</totalRounds>
          <completedTricks>{state.completedTricks}</completedTricks>
          {state.currentTrump.map(c => <trump>{c.toString}</trump>).getOrElse(NodeSeq.Empty)}
        </meta>
        <players>{playersXml}</players>
        {deckXml}
        {trickXml.getOrElse(NodeSeq.Empty)}
      </game>

    XML.save(file.getAbsolutePath, xml, "UTF-8", xmlDecl = true)
  }

  // -------------------------
  // LOAD
  // -------------------------
  override def load(): GameState = {

    if (!file.exists())
      return GameState.empty

    val xml = XML.loadFile(file)

    def cardFromXml(n: Node): Card =
      (n \@ "type") match
        case "normal" =>
          NormalCard(
            CardColor.valueOf(n \@ "color"),
            (n \@ "value").toInt
          )
        case "wizard" =>
          WizardCard(CardColor.valueOf(n \@ "color"))
        case "joker" =>
          JokerCard(CardColor.valueOf(n \@ "color"))
        case other =>
          throw new IllegalArgumentException(s"Unknown card type: $other")

    val players =
      (xml \ "players" \ "player").map { p =>
        Player(
          id = (p \ "id").text.toInt,
          hand = (p \ "hand" \ "card").map(cardFromXml).toList,
          totalPoints = (p \ "points").text.toInt,
          predictedTricks = (p \ "predicted").text.toInt,
          tricks = (p \ "won").text.toInt
        )
      }.toList

    val deck =
      Deck((xml \ "deck" \ "card").map(cardFromXml).toList)

    val trick =
      (xml \ "trick").headOption.map { t =>
        Trick(
          (t \ "move").map { m =>
            (m \@ "player").toInt ->
              cardFromXml((m \ "card").head)
          }.toMap
        )
      }

    GameState(
      amountOfPlayers = (xml \ "meta" \ "amountOfPlayers").text.toInt,
      players = players,
      deck = deck,
      currentRound = (xml \ "meta" \ "currentRound").text.toInt,
      totalRounds = (xml \ "meta" \ "totalRounds").text.toInt,
      completedTricks = (xml \ "meta" \ "completedTricks").text.toInt,
      currentTrump =
        (xml \ "meta" \ "trump").headOption.map(n => CardColor.valueOf(n.text)),
      currentTrick = trick
    )
  }
}
