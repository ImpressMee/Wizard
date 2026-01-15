package de.htwg.wizard.view

import scalafx.application.Platform
import scalafx.scene.{Parent, Scene}
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.stage.Stage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.{Image, ImageView}
import de.htwg.wizard.control.{ContinueAfterRound, GameEvent, GameFinished, GameLoadAvailable, GamePort, LoadGame, Observer, PlayerAmountRequested, PlayerAmountSelected, PredictionsRequested, PredictionsSubmitted, RoundFinished, TrickMoveRequested, TrickMovesSubmitted}
import de.htwg.wizard.model.*
import de.htwg.wizard.model.modelComponent.{Card, CardType, GameState, Player, Trick}
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

  private var loadAvailable: Boolean = false //FIO

  // =========================================================
  // Entry
  // =========================================================

  def showStart(primaryStage: Stage): Unit =
    stage = primaryStage
    stage.getIcons.add(new Image(icon))
    game.init()   //FIO
    stage.scene = startScene()

    stage.onCloseRequest = event => {
      if !game.canSafelyExit then
        event.consume()
        showUnsafeExitDialog()
    }



  // =========================================================
  // Observer
  // =========================================================

  override def update(event: GameEvent): Unit =
    Platform.runLater {
      event match
        case GameLoadAvailable(avail, _) =>
          loadAvailable = avail
          stage.scene = startScene()
          //FIO
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
  //FIO
  private val loadButton =
    new Button("Load Game") {
      style =
        "-fx-background-color: #1f7a1f;" + // waldgrün
          "-fx-text-fill: white;" +
          "-fx-font-size: 16px;" +
          "-fx-font-weight: bold;"
      onAction = _ => game.handleInput(LoadGame)
    }

  private def startScene(): Scene =
    styledScene(
      new BorderPane {
        styleClass += "start-scene"

        top = new VBox {
          alignment = Pos.TopCenter
          padding = Insets(40, 5, 20, 0)
          children = Seq(
            new Label("WIZARD") {
              styleClass += "title"
            }
          )
        }

        center = new VBox {
          alignment = Pos.Center
          spacing = 15
          children =
            Seq(
              new Button("Enter") {
                onAction = _ => game.startGame()
              }
            ) ++
              (if loadAvailable then Seq(loadButton) else Seq())
        }
      }
    )




  // =========================================================
  // Player count
  // =========================================================

  private def playerCountScene(): Scene =
    styledScene(
      new VBox {
        styleClass += "player-count-scene"
        alignment = Pos.Center
        spacing = 15
        children =
          Seq(new Label("How Many Players Are Playing?") {
            styleClass += "pre-game-title"
          }) ++
            Seq(3, 4, 5, 6).map { n =>
              new Button(s"$n Players") {
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
        styleClass += "prediction-scene"
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

  private def gameBoardScene(state: GameState): Scene = {

    val activePlayer =
      state.players.find(p => !trickMoves.contains(p.id))
        .getOrElse(state.players.head)

    val virtualState =
      if trickMoves.nonEmpty then
        state.copy(
          currentTrick = Some(
            Trick(
              trickMoves.map { case (pid, idx) =>
                pid -> state.players.find(_.id == pid).get.hand(idx)
              }
            )
          )
        )
      else
        state

    styledScene(
      new BorderPane {
        styleClass += "playing-scene"
        top = new VBox {
          styleClass += "player-info"
          padding = Insets(10)
          children = Seq(
            new Label(s"Round: ${state.currentRound}"),
            new Label(s"Trump: ${state.currentTrump.getOrElse("ERROR")}"){
              styleClass += "trump-label"
            },
            new Label(s"Trick: ${state.completedTricks + 1}"),
            new Label(s"Active Player: Player ${activePlayer.id}")
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
                cardNode(card, cardWidthTrick)
              }
        }

        bottom = new VBox {
          alignment = Pos.Center
          spacing = 10
          padding = Insets(0, 0, 60, 0)
          children = Seq(
            new Label(s"Player ${activePlayer.id}, Choose A Card:") {
              styleClass += "text-label"
              style = "-fx-font-weight: bold"
            },
            new HBox {
              styleClass += "center-row"
              children =
                activePlayer.hand.zipWithIndex.map { (card, idx) =>

                  val allowed =
                    game.isAllowedMove(activePlayer.id, idx, virtualState)

                  new Button {
                    graphic = cardNode(card, cardWidthHand)
                    styleClass += "card-button"

                    disable = !allowed
                    opacity = if allowed then 1.0 else 0.4

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
  }

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
                    new Label(s"Tricks Predicted: ${p.predictedTricks}"),
                    new Label(s"Actual Tricks:    ${p.tricks}"),
                    new Label(s"=> Total Points:  ${p.totalPoints}"),
                  )
                }
              }
          },
          new Button("Continue") {
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
          new Button("Play Again") {
            onAction = _ => showStart(stage)
          },
          new Button("Exit") {
            onAction = _ => Platform.exit()
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
    new StackPane {
      prefWidth = cardWidth
      prefHeight = cardWidth * 1.45
      children = Seq(cardImage(card, cardWidth))
    }

  private def showUnsafeExitDialog(): Unit = {
    val alert = new Alert(Alert.AlertType.Warning) {
      title = "Unsafe Exit"
      headerText = "Closing the game during a round"
      contentText =
        "Closing the game during an active round may lead to inconsistent or corrupted save data.\n\n" +
          "You can safely exit the game after the current round has finished."
    }
    alert.initOwner(stage)
    alert.showAndWait()
  }

}