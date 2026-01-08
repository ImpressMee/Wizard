package de.htwg.wizard.view

import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.stage.Stage
import scalafx.geometry.{Pos, Insets}

import de.htwg.wizard.component.game.GamePort
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.input.*
import de.htwg.wizard.control.observer.Observer
import de.htwg.wizard.model.*

class GuiView(game: GamePort) extends Observer {

  private var stage: Stage = _

  private var predictionIndex = 0
  private var predictions: Map[Int, Int] = Map.empty
  private var trickMoves: Map[Int, Int] = Map.empty

  // =========================================================
  // Entry
  // =========================================================

  def showStart(primaryStage: Stage): Unit =
    stage = primaryStage
    stage.scene = startScene()

  // =========================================================
  // Observer
  // =========================================================

  override def update(event: GameEvent): Unit =
    Platform.runLater {
      event match
        case PlayerAmountRequested(_) =>
          stage.scene = playerCountScene()

        case PredictionsRequested(state) =>
          predictionIndex = 0
          predictions = Map.empty
          stage.scene = predictionScene(state)

        case TrickMoveRequested(_, state) =>
          trickMoves = Map.empty
          stage.scene = gameBoardScene(state)

        case RoundFinished(state) =>
          stage.scene = roundSummaryScene(state)

        case GameFinished(winner, state) =>
          stage.scene = endScene(winner, state)

        case _ => ()
    }

  // =========================================================
  // Scenes
  // =========================================================

  private def startScene(): Scene =
    new Scene(800, 800) {
      root = new VBox {
        alignment = Pos.Center
        spacing = 30
        children = Seq(
          new Label("WIZARD"),
          new Button("Start") {
            onAction = _ => game.startGame()
          }
        )
      }
    }

  private def playerCountScene(): Scene =
    new Scene(800, 800) {
      root = new VBox {
        alignment = Pos.Center
        spacing = 15
        children =
          Seq(new Label("Spieleranzahl")) ++
            Seq(3, 4, 5, 6).map { n =>
              new Button(s"$n Spieler") {
                onAction = _ =>
                  game.handleInput(PlayerAmountSelected(n))
              }
            } ++
            Seq(
              new Button("Back") {
                onAction = _ => showStart(stage)
              }
            )
      }
    }

  private def predictionScene(state: GameState): Scene = {
    val player = state.players(predictionIndex)

    new Scene(800, 800) {
      root = new VBox {
        alignment = Pos.Center
        spacing = 20
        children = Seq(
          new Label(s"Player ${player.id}: How many tricks will you make?"),

          new HBox {
            spacing = 12
            alignment = Pos.Center
            children =
              player.hand.map { card =>
                new Label(card.toString) {
                  style =
                    s"-fx-font-weight: bold; -fx-text-fill: ${colorOf(card)};"
                }
              }
          },

          new HBox {
            spacing = 10
            alignment = Pos.Center
            children =
              (0 to player.hand.size).map { n =>
                new Button(n.toString) {
                  onAction = _ =>
                    predictions += player.id -> n
                    predictionIndex += 1

                    if predictionIndex < state.players.size then
                      stage.scene = predictionScene(state)
                    else
                      game.handleInput(
                        PredictionsSubmitted(predictions)
                      )
                }
              }
          }
        )
      }
    }
  }

  private def gameBoardScene(state: GameState): Scene = {

    val activePlayer =
      state.players.find(p => !trickMoves.contains(p.id)).get

    new Scene(800, 800) {
      root = new BorderPane {

        top = new VBox {
          spacing = 5
          padding = Insets(10)
          children = Seq(
            new Label(s"Runde: ${state.currentRound}"),
            new Label(s"Trumpf: ${state.currentTrump.getOrElse("Kein")}"),
            new Label(s"Stich: ${state.completedTricks + 1}"),
            new Label(s"Aktiver Spieler: Player ${activePlayer.id}")
          )
        }

        center = new HBox {
          spacing = 20
          alignment = Pos.Center
          children =
            trickMoves.toSeq
              .sortBy(_._1)
              .map { (pid, idx) =>
                val card =
                  state.players.find(_.id == pid).get.hand(idx)
                new Label(card.toString) {
                  style =
                    s"-fx-font-size: 16; -fx-text-fill: ${colorOf(card)};"
                }
              }
        }

        bottom = new VBox {
          alignment = Pos.Center
          spacing = 10
          padding = Insets(0, 0, 60, 0)

          children = Seq(
            new Label(s"Player ${activePlayer.id}, wähle eine Karte:"),

            new HBox {
              spacing = 10
              alignment = Pos.Center
              children =
                activePlayer.hand.zipWithIndex.map { (card, idx) =>
                  new Button(card.toString) {

                    disable =
                      !isAllowedGuiMove(card, activePlayer, trickMoves, state)

                    style =
                      s"""
                         |-fx-font-weight: bold;
                         |-fx-text-fill: ${colorOf(card)};
                         |""".stripMargin

                    onAction = _ =>
                      trickMoves += activePlayer.id -> idx

                      if trickMoves.size == state.players.size then
                        game.handleInput(
                          TrickMovesSubmitted(trickMoves)
                        )
                      else
                        stage.scene = gameBoardScene(state)
                  }
                }
            }
          )
        }
      }
    }
  }

  private def roundSummaryScene(state: GameState): Scene =
    new Scene(800, 800) {
      root = new VBox {
        alignment = Pos.Center
        spacing = 20
        children = Seq(
          new Label(s"Round ${state.currentRound} finished"),

          new VBox {
            spacing = 10
            children =
              state.players.map { p =>
                new VBox {
                  spacing = 2
                  children = Seq(
                    new Label(s"Player ${p.id}"),
                    new Label(s"tricks predicted: ${p.predictedTricks}"),
                    new Label(s"actual tricks:    ${p.tricks}"),
                    new Label(s"=> total points:  ${p.totalPoints}"),
                    new Separator()
                  )
                }
              }
          },

          new Button("Weiter") {
            onAction = _ =>
              game.handleInput(ContinueAfterRound)
          }
        )
      }
    }

  // =========================================================
  // Final winner scene (winner + all players)
  // =========================================================

  private def endScene(winner: Player, state: GameState): Scene =
    new Scene(800, 800) {
      root = new VBox {
        alignment = Pos.Center
        spacing = 20
        children = Seq(

          new Label(s"Winner: Player ${winner.id}") {
            style = "-fx-font-size: 18; -fx-font-weight: bold;"
          },

          new Label(s"Points: ${winner.totalPoints}") {
            style = "-fx-font-size: 16;"
          },

          new Separator(),

          new Label("Final scores:"),

          new VBox {
            spacing = 5
            children =
              state.players
                .sortBy(-_.totalPoints)
                .map { p =>
                  new Label(s"Player ${p.id}: ${p.totalPoints} points")
                }
          },

          new Separator(),

          new Button("Ende") {
            onAction = _ => showStart(stage)
          }
        )
      }
    }

  // =========================================================
  // Helper
  // =========================================================

  private def colorOf(card: Card): String =
    card.color match
      case CardColor.Red    => "red"
      case CardColor.Blue   => "blue"
      case CardColor.Green  => "green"
      case CardColor.Yellow => "goldenrod"

  private def isAllowedGuiMove(
                                card: Card,
                                player: Player,
                                trickMoves: Map[Int, Int],
                                state: GameState
                              ): Boolean = {

    if trickMoves.isEmpty then return true

    val leadColorOpt =
      trickMoves.headOption.flatMap { (pid, idx) =>
        state.players.find(_.id == pid).map(_.hand(idx)).collect {
          case c if isNormal(c) => c.color
        }
      }

    leadColorOpt.forall { lead =>
      !player.hand.exists(c => isNormal(c) && c.color == lead) ||
        (isNormal(card) && card.color == lead)
    }
  }
}
