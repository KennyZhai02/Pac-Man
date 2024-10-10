package pacman

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.animation.AnimationTimer
import scalafx.Includes._
import scalafx.scene.control.{Label, Menu, MenuBar, MenuItem}
import scalafx.scene.layout.VBox

object MainApp extends JFXApp {
  private val startX = 13
  private val startY = 23
  val pacMan = new Pac_Man(startX, startY)

  // Create ghosts with speed adjustment (slower)
  private val ghosts = List(
    new Blinky(14, 12),
    new Pinky(14, 13),
    new Inky(14, 14),
    new Clyde(14, 15)
  )

  // Instantiate GameMap without the FXML path (it is handled internally)
  val gameMap = new GameMap()

  // Score and lives labels
  private val scoreLabel = new Label(s"Score: ${pacMan.score}")
  private val livesLabel = new Label(s"Lives: ${pacMan.lives}")

  // Main game loop for continuous updates
  private var gameLoop: AnimationTimer = null
  private var isGamePaused = false

  // Menu bar
  private val menuBar = new MenuBar {
    menus = List(
      new Menu("Game") {
        items = List(
          new MenuItem("Pause") {
            onAction = _ => pauseGame()
          },
          new MenuItem("Resume") {
            onAction = _ => resumeGame()
          },
          new MenuItem("Exit") {
            onAction = _ => System.exit(0)
          }
        )
      }
    )
  }

  // Initial stage setup
  stage = new JFXApp.PrimaryStage {
    title = "Pac-Man"
    scene = new Scene {
      content = new VBox {
        children.addAll(menuBar, scoreLabel, livesLabel, gameMap.getMapPane)
      }

      // Update Pac-Man's initial position on the map
      gameMap.updatePacManPosition(pacMan)

      // Update ghosts' initial positions on the map
      ghosts.foreach(gameMap.updateGhostPosition)

      // Handle key press events for moving Pac-Man
      onKeyPressed = (event: KeyEvent) => {
        val (dx, dy) = event.code match {
          case KeyCode.W | KeyCode.Up    => (0, -1)
          case KeyCode.S | KeyCode.Down  => (0, 1)
          case KeyCode.A | KeyCode.Left  => (-1, 0)
          case KeyCode.D | KeyCode.Right => (1, 0)
          case _ => (0, 0)
        }

        // Move Pac-Man if the intended move is valid
        if (gameMap.isValidMove(pacMan.x + dx, pacMan.y + dy)) {
          pacMan.move(dx, dy)
          gameMap.updatePacManPosition(pacMan)
        }
      }
    }
  }

  // Start the game loop
  restartGame()

  // Restart the game
  private def restartGame(): Unit = {
    // Reset game state
    pacMan.reset(startX, startY)
    pacMan.score = 0
    pacMan.lives = 3
    ghosts.foreach { ghost =>
      ghost.x = 14
      ghost.y = 12 + ghosts.indexOf(ghost)
      gameMap.updateGhostPosition(ghost)
    }

    if (gameLoop == null) {
      gameLoop = AnimationTimer { now =>
        moveGhosts()  // Update ghost movement
        updateLabels()  // Update score and lives
        checkCollisions()  // Check for collisions between Pac-Man and ghosts
        checkGameOver()  // Check if game over condition is met
      }
      gameLoop.start()
    }
    isGamePaused = false
  }

  // Pause the game
  private def pauseGame(): Unit = {
    if (gameLoop != null) {
      gameLoop.stop()
      isGamePaused = true
    }
  }

  // Resume the game
  private def resumeGame(): Unit = {
    if (gameLoop != null && isGamePaused) {
      gameLoop.start()
      isGamePaused = false
    }
  }

  // Method to move ghosts
  private def moveGhosts(): Unit = {
    ghosts.foreach { ghost =>
      val (dx, dy) = ghost.decideMove(pacMan.x, pacMan.y, gameMap)
      if (gameMap.isValidMove(ghost.x + dx, ghost.y + dy)) {
        ghost.move(dx, dy)
        gameMap.updateGhostPosition(ghost)
      }
    }
  }

  // Update score and lives display
  private def updateLabels(): Unit = {
    scoreLabel.text = s"Score: ${pacMan.score}"
    livesLabel.text = s"Lives: ${pacMan.lives}"
  }

  // Check for collisions between Pac-Man and any ghost
  private def checkCollisions(): Unit = {
    ghosts.foreach { ghost =>
      if (ghost.x == pacMan.x && ghost.y == pacMan.y) {
        pacMan.loseLife()
        resetPositions()
      }
    }
  }

  // Reset Pac-Man and ghost positions after a life is lost
  private def resetPositions(): Unit = {
    pacMan.reset(startX, startY)
    gameMap.updatePacManPosition(pacMan)

    // Reset all ghosts to their original positions
    ghosts.foreach { ghost =>
      ghost.x = 14
      ghost.y = 12 + ghosts.indexOf(ghost)
      gameMap.updateGhostPosition(ghost)
    }
  }

  // Check if game over condition is met (either Pac-Man is out of lives or all dots are eaten)
  private def checkGameOver(): Unit = {
    if (pacMan.lives <= 0 || gameMap.areAllDotsEaten) {
      gameLoop.stop()
      val gameOverLabel = new Label(
        if (pacMan.lives <= 0) "Game Over!" else "You Win!"
      )
      stage.scene = new Scene {
        content = new VBox {
          children.addAll(gameOverLabel, scoreLabel)
        }
      }
    }
  }
}