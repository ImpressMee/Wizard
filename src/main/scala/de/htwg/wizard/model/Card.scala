package de.htwg.wizard.model

import scala.util.Random

// ==============================
// CardType
// ==============================
enum CardType:
  case Normal(value: Int)
  case Wizard
  case Joker

// ==============================
// CardColor
// ==============================
enum CardColor:
  case Red, Green, Blue, Yellow

// ==============================
// Base Card
// ==============================
sealed trait Card:
  def color: CardColor
  def cardType: CardType

// ==============================
// Cards
// ==============================
case class NormalCard(color: CardColor, value: Int) extends Card:
  val cardType: CardType = CardType.Normal(value)

case class WizardCard(color: CardColor) extends Card:
  val cardType: CardType = CardType.Wizard

case class JokerCard(color: CardColor) extends Card:
  val cardType: CardType = CardType.Joker

// ==============================
// Helpers
// ==============================
def isWizard(c: Card): Boolean = c.cardType == CardType.Wizard
def isJoker(c: Card): Boolean  = c.cardType == CardType.Joker
def isNormal(c: Card): Boolean = c.cardType match
  case CardType.Normal(_) => true
  case _ => false

def value(c: Card): Int = c.cardType match
  case CardType.Normal(v) => v
  case _ => 0

def determinesColor(card: Card): Boolean =
  isNormal(card)

def trumpColor(): CardColor =
  CardColor.values(Random.nextInt(CardColor.values.length))

// ==============================
// Factory
// ==============================
object CardFactory:
  def apply(color: CardColor, kind: String): Card =
    kind.toLowerCase match
      case "wizard" => WizardCard(color)
      case "joker"  => JokerCard(color)
      case other =>
        throw new IllegalArgumentException(s"Unknown card type: $other")

  def apply(color: CardColor, value: Int): Card =
    NormalCard(color, value)
