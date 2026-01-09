package de.htwg.wizard.view

import scalafx.application.Platform
import scalafx.scene.{Parent, Scene}
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.{Image, ImageView}
import de.htwg.wizard.component.game.GamePort
import de.htwg.wizard.control.event.*
import de.htwg.wizard.control.input.*
import de.htwg.wizard.control.observer.Observer
import de.htwg.wizard.model.*
import scalafx.Includes.observableList2ObservableBuffer

class GuiView(game: GamePort) extends Observer {

  // =========================================================
  // Constants / State
  // =========================================================


  private val Width  = 1200
  private val Height = 800

  private val cardWidthHand  = 130.0
  private val cardWidthTrick = 150.0

  private val icon = "/cards/Icon.jpg"

  private var stage: Stage = _
  private var predictionIndex = 0
  private var predictions: Map[Int, Int] = Map.empty
  private var trickMoves: Map[Int, Int] = Map.empty
  // =========================================================
  // Entry
  // =========================================================

  def showStart(primaryStage: Stage): Unit =
    stage = primaryStage
    stage.getIcons.add(new Image(icon))
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
  // Scene helper
  // =========================================================


  private def styledScene(root: Parent): Scene =
    val scene = new Scene(root, Width, Height)
    scene.stylesheets += getClass.getResource("/style/gui.css").toExternalForm
    scene
  // =========================================================
  // Start
  // =========================================================

  private def startScene(): Scene =
    styledScene(
      new StackPane {
        children = Seq(
          new VBox {
            styleClass += "start-background"
            alignment = Pos.Center
            spacing = 30
            children = Seq(
              new Label("WIZARD") { styleClass += "title" },
              new Button("Start") {
                styleClass += "sbutton"
                onAction = _ => game.startGame()
              }
            )
          }
        )
      }
    )


  // =========================================================
  // Player count
  // =========================================================

  private def playerCountScene(): Scene =
    styledScene(
      new VBox {
        styleClass += "game-background"
        alignment = Pos.Center
        spacing = 15
        children =
          Seq(new Label("How Many Players Are Playing?") {
            styleClass += "pre-game-title"
          }) ++
            Seq(3, 4, 5, 6).map { n =>
              new Button(s"$n Spieler") {
                onAction = _ => game.handleInput(PlayerAmountSelected(n))
              }
            } :+
            new Button("Back") {
              onAction = _ => showStart(stage)
            }
      }
    )

  // =========================================================
  // Prediction
  // =========================================================


  private def predictionScene(state: GameState): Scene =
    val player = state.players(predictionIndex)
    styledScene(
      new VBox {
        styleClass += "game-background"
        alignment = Pos.Center
        spacing = 20
        children = Seq(
          new Label(s"Player ${player.id}: How Many Tricks Will You Make?") {
            styleClass += "pre-game-title"
          },
          new HBox {
            styleClass += "center-row"
            children = player.hand.map(card => cardNode(card, cardWidthHand))
          },
          new HBox {
            alignment = Pos.Center
            spacing = 10
            children =
              (0 to player.hand.size).map { n =>
                new Button(n.toString) {
                  onAction = _ =>
                    predictions += player.id -> n
                    predictionIndex += 1
                    if predictionIndex < state.players.size then
                      stage.scene = predictionScene(state)
                    else
                      game.handleInput(PredictionsSubmitted(predictions))
                }
              }
          }
        )
      }
    )

  // =========================================================
  // Game board
  // =========================================================

  private def gameBoardScene(state: GameState): Scene =
    val activePlayer =
      state.players.find(p => !trickMoves.contains(p.id)).get
    styledScene(
      new BorderPane {
        styleClass += "game-background"
        top = new VBox {
          styleClass += "player-info"
          padding = Insets(10)
          children = Seq(
            new Label(s"Runde: ${state.currentRound}"),
            new Label(s"Trumpf: ${state.currentTrump.getOrElse("Kein")}"){
              styleClass += "trump-label"
            },
            new Label(s"Stich: ${state.completedTricks + 1}"),
            new Label(s"Aktiver Spieler: Player ${activePlayer.id}")
          )
        }
        center = new HBox {
          spacing = 20
          alignment = Pos.Center
          prefHeight = 200
          maxHeight = 200
          styleClass += "playingfield"
          children =
            trickMoves.toSeq
              .sortBy(_._1)
              .map { (pid, idx) =>
                val card =
                  state.players.find(_.id == pid).get.hand(idx)
                cardNode(card, cardWidthTrick)
              }
        }
        bottom = new VBox {
          alignment = Pos.Center
          spacing = 10
          padding = Insets(0, 0, 60, 0)
          children = Seq(
            new Label(s"Player ${activePlayer.id}, wähle eine Karte:") {
              styleClass += "text-label"
              style = "-fx-font-weight: bold"
            },
            new HBox {
              styleClass += "center-row"
              styleClass += "text-label"
              children =
                activePlayer.hand.zipWithIndex.map { (card, idx) =>
                  new Button {
                    graphic = cardNode(card, cardWidthHand)
                    styleClass += "card-button"
                    disable = !game.isAllowedMove(activePlayer.id, idx, state)
                    onAction = _ =>
                      trickMoves += activePlayer.id -> idx
                      if trickMoves.size == state.players.size then
                        game.handleInput(TrickMovesSubmitted(trickMoves))
                      else
                        stage.scene = gameBoardScene(state)
                  }
                }
            }
          )
        }
      }
    )

  // =========================================================
  // Summary
  // =========================================================

  private def roundSummaryScene(state: GameState): Scene =
    styledScene(
      new VBox {
        styleClass += "stats-background"
        styleClass += "center-row"
        alignment = Pos.Center
        spacing = 20
        children = Seq(
          new Label(s"Round ${state.currentRound} finished") {
            styleClass += "round-summary-title"
          },
          new VBox {
            spacing = 10
            padding = Insets(20)
            children =
              state.players.map { p =>
                new VBox {
                  styleClass += "center-row"
                  styleClass += "summary-box"
                  styleClass += "text-label"
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
            onAction = _ => game.handleInput(ContinueAfterRound)
          }
        )
      }
    )
  // =========================================================
  // End
  // =========================================================

  private def endScene(winner: Player, state: GameState): Scene =
    styledScene(
      new VBox {
        styleClass += "stats-background"
        alignment = Pos.Center
        spacing = 20
        children = Seq(
          new Label(s"Winner: Player ${winner.id}") {
            styleClass += "winner-title"
          },
          new Label(s"Points: ${winner.totalPoints}") {
            styleClass += "text-label"
          },
          new Separator(),
          new Label("Final scores:") {
            styleClass += "text-label"
          },
          new VBox {
            styleClass += "center-row"
            spacing = 5
            children =
              state.players
                .sortBy(-_.totalPoints)
                .map(p =>
                  new Label(s"Player ${p.id}: ${p.totalPoints} points") {
                    styleClass += "text-label"
                  }
                )
          },
          new Separator(),
          new Button("Ende") {
            onAction = _ => showStart(stage)
          }
        )
      }
    )

  // =========================================================
  // Card helpers (pure UI)
  // =========================================================

  private def cardImage(card: Card, width: Double): ImageView =

    val fileName =
      card.cardType match
        case CardType.Normal(v) => s"${card.color.toString.toUpperCase}$v.png"
        case CardType.Wizard => s"${card.color.toString.toUpperCase}Wizard.png"
        case CardType.Joker => s"${card.color.toString.toUpperCase}Joker.png"

    val url =
      Option(getClass.getResource(s"/cards/$fileName"))
        .getOrElse(getClass.getResource("/cards/NaN.png"))

    new ImageView(new Image(url.toExternalForm)) {
      fitWidth = width
      preserveRatio = true
      smooth = true
    }

  private def cardNode(card: Card, cardWidth: Double): StackPane =

    val label = new Label(
      card.cardType match
        case CardType.Normal(v) => s"${card.color} $v"
        case CardType.Wizard => s"${card.color} Wizard"
        case CardType.Joker => s"${card.color} Joker"
    )
    label.styleClass += "card-label"
    StackPane.setAlignment(label, Pos.TopCenter)

    new StackPane {
      prefWidth = cardWidth
      prefHeight = cardWidth * 1.45
      children = Seq(cardImage(card, cardWidth), label)
    }
}