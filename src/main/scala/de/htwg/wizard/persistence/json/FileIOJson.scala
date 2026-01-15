package de.htwg.wizard.persistence.json

import de.htwg.wizard.persistence.FileIO
import de.htwg.wizard.model.modelComponent.*
import de.htwg.wizard.model.*
import play.api.libs.json.*
import java.io.File
import scala.io.Source
import java.io.PrintWriter

class FileIOJson extends FileIO {

  override def hasSave: Boolean =
    file.exists()

  
  private val file = new File("wizard.json")

  // ---------------------------------------------------------
  // JSON Formats
  // ---------------------------------------------------------

  given cardColorFormat: Format[CardColor] =
    Format(
      Reads.StringReads.map(s =>
        CardColor.values.find(_.toString == s)
          .getOrElse(throw new IllegalArgumentException(s"Unknown color: $s"))
      ),
      Writes(c => JsString(c.toString))
    )

  given cardFormat: Format[Card] = new Format[Card] {

    override def writes(card: Card): JsValue = card match
      case NormalCard(color, value) =>
        Json.obj(
          "type" -> "normal",
          "color" -> color,
          "value" -> value
        )
      case WizardCard(color) =>
        Json.obj(
          "type" -> "wizard",
          "color" -> color
        )
      case JokerCard(color) =>
        Json.obj(
          "type" -> "joker",
          "color" -> color
        )

    override def reads(json: JsValue): JsResult[Card] =
      (json \ "type").as[String] match
        case "normal" =>
          JsSuccess(
            NormalCard(
              (json \ "color").as[CardColor],
              (json \ "value").as[Int]
            )
          )
        case "wizard" =>
          JsSuccess(
            WizardCard((json \ "color").as[CardColor])
          )
        case "joker" =>
          JsSuccess(
            JokerCard((json \ "color").as[CardColor])
          )
        case other =>
          JsError(s"Unknown card type: $other")
  }

  given playerFormat: OFormat[Player] = Json.format[Player]
  given deckFormat:   OFormat[Deck]   = Json.format[Deck]
  given trickFormat:  OFormat[Trick]  = Json.format[Trick]
  given gameStateFormat: OFormat[GameState] = Json.format[GameState]

  // ---------------------------------------------------------
  // SAVE
  // ---------------------------------------------------------
  override def save(state: GameState): Unit = {
    val json = Json.prettyPrint(Json.toJson(state))
    val pw = new PrintWriter(file)
    pw.write(json)
    pw.close()
  }

  // ---------------------------------------------------------
  // LOAD
  // ---------------------------------------------------------
  override def load(): GameState = {
    if (!file.exists())
      return GameState.empty

    val source = Source.fromFile(file)
    val jsonString = source.getLines().mkString
    source.close()

    Json.parse(jsonString).as[GameState]
  }
}
