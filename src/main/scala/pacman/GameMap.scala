package pacman

import javafx.scene.layout.{GridPane, Pane}
import javafx.scene.shape.{Rectangle, Circle}
import javafx.scene.paint.Color
import javafx.fxml.FXMLLoader
import java.io.IOException
import scala.collection.mutable

class GameMap {
  // Map grid size
  val width = 28
  val height = 31  // Adjust the height to match the number of rows in the mapLayout

  // Reinstate the map array (walls, dots, and power pellets)
  private val mapLayout = Array(
    "############################",
    "#............##............#",
    "#.####.#####.##.#####.####.#",
    "#.####.#####.##.#####.####.#",
    "#.####.#####.##.#####.####.#",
    "#..........................#",
    "#.####.##.########.##.####.#",
    "#.####.##.########.##.####.#",
    "#......##....##....##......#",
    "######.##### ## #####.######",
    "     #.##### ## #####.#     ",
    "     #.##          ##.#     ",
    "     #.## ###--### ##.#     ",
    "######.## #      # ##.######",
    "      .   #      #   .      ",
    "######.## #      # ##.######",
    "     #.## ######## ##.#     ",
    "     #.##          ##.#     ",
    "     #.## ######## ##.#     ",
    "######.## ######## ##.######",
    "#............##............#",
    "#.####.#####.##.#####.####.#",
    "#.####.#####.##.#####.####.#",
    "#...##................##...#",
    "###.##.##.########.##.##.###",
    "###.##.##.########.##.##.###",
    "#......##....##....##......#",
    "#.##########.##.##########.#",
    "#.##########.##.##########.#",
    "#..........................#",
    "############################"
  )

  // FXML grid layout where the map will be drawn
  private var mapPane: GridPane = _

  // Store the Pac-Man and ghost nodes
  private var pacManNode: Pane = _
  private var ghostNodes: mutable.Map[String, Pane] = mutable.Map.empty

  private var dots: Array[Array[Option[Circle]]] = Array.fill(height, width)(None)
  private var powerPellets: List[(Int, Int)] = List()

  try {
    // Load the FXML file
    val loader = new FXMLLoader(getClass.getResource("/FXML/Map.fxml"))
    loader.setController(this)
    mapPane = loader.load[GridPane]()
  } catch {
    case e: IOException =>
      println(s"Error loading Map.fxml: ${e.getMessage}")
      e.printStackTrace()
      mapPane = new GridPane()  // Fallback in case FXML load fails
  }

  // Initialize Pac-Man node by loading its FXML
  try {
    val pacManLoader = new FXMLLoader(getClass.getResource("/FXML/Pac_Man.fxml"))
    pacManNode = pacManLoader.load[Pane]()
    mapPane.add(pacManNode, 13, 17)  // Initial position for Pac-Man
  } catch {
    case e: IOException =>
      println(s"Error loading Pac_Man.fxml: ${e.getMessage}")
  }

  // Load ghost nodes dynamically and place them on the map
  val ghosts = List(
    ("Blinky", "/FXML/Blinky.fxml", 14, 12),
    ("Pinky", "/FXML/Pinky.fxml", 14, 13),
    ("Inky", "/FXML/Inky.fxml", 14, 14),
    ("Clyde", "/FXML/Clyde.fxml", 14, 15)
  )

  try {
    for ((name, fxmlPath, x, y) <- ghosts) {
      val ghostLoader = new FXMLLoader(getClass.getResource(fxmlPath))
      val ghostNode = ghostLoader.load[Pane]()
      ghostNodes += (name -> ghostNode)
      mapPane.add(ghostNode, x, y)
    }
  } catch {
    case e: IOException =>
      println(s"Error loading ghost FXML: ${e.getMessage}")
      e.printStackTrace()
  }

  // Initialize the dots and pellets based on the map array
  initializeMapElements()

  // Method to dynamically initialize the dots and power pellets based on the map array
  private def initializeMapElements(): Unit = {
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        mapLayout(y)(x) match {
          case '#' =>  // Wall
            val wall = new Rectangle {
              setWidth(20)
              setHeight(20)
              setFill(Color.BLUE)  // Represent walls as blue rectangles
              setStroke(Color.BLUE)
            }
            mapPane.add(wall, x, y)

          case '.' =>  // Regular dot
            val dot = new Circle {
              setRadius(2.5)
              setFill(Color.WHITE)  // Small dots are white
            }
            dots(y)(x) = Some(dot)
            mapPane.add(dot, x, y)

          case 'o' =>  // Power pellet
            val pellet = new Circle {
              setRadius(5)
              setFill(Color.YELLOW)  // Power pellets are yellow and larger
            }
            powerPellets = powerPellets :+ (x, y)
            mapPane.add(pellet, x, y)

          case _ =>  // Empty space (do nothing)
        }
      }
    }
  }

  // Method to update Pac-Man's position on the map
  def updatePacManPosition(pacMan: Pac_Man): Unit = {
    if (mapPane != null && pacManNode != null) {
      GridPane.setColumnIndex(pacManNode, pacMan.x)
      GridPane.setRowIndex(pacManNode, pacMan.y)
      eatDotOrPellet(pacMan)
    }
  }

  // Method for Pac-Man to eat dots or power pellets
  def eatDotOrPellet(pacMan: Pac_Man): Unit = {
    if (pacMan.x >= 0 && pacMan.x < width && pacMan.y >= 0 && pacMan.y < height) {
      dots(pacMan.y)(pacMan.x) match {
        case Some(dot) =>
          mapPane.getChildren.remove(dot)
          dots(pacMan.y)(pacMan.x) = None
          pacMan.eatDot()
        case None =>
          if (powerPellets.contains((pacMan.x, pacMan.y))) {
            mapPane.getChildren.removeIf(node =>
              GridPane.getColumnIndex(node) == pacMan.x &&
                GridPane.getRowIndex(node) == pacMan.y &&
                node.isInstanceOf[Circle] &&
                node.asInstanceOf[Circle].getRadius == 5
            )
            powerPellets = powerPellets.filter(_ != (pacMan.x, pacMan.y))
            pacMan.eatPowerPellet()
          }
      }
    }
  }

  // Method to update ghost positions on the map
  def updateGhostPosition(ghost: Ghost): Unit = {
    if (mapPane != null && ghostNodes.contains(ghost.name)) {
      ghostNodes.get(ghost.name).foreach { ghostNode =>
        GridPane.setColumnIndex(ghostNode, ghost.x)
        GridPane.setRowIndex(ghostNode, ghost.y)
      }
    }
  }

  // Check if the given move is valid (i.e., not a wall or out of bounds)
  def isValidMove(x: Int, y: Int): Boolean = {
    x >= 0 && x < width && y >= 0 && y < height && mapLayout(y)(x) != '#'
  }

  // Method to check if all dots have been eaten
  def areAllDotsEaten: Boolean = dots.flatten.forall(_.isEmpty) && powerPellets.isEmpty

  // Return the GridPane to be rendered in the main app
  def getMapPane: GridPane = mapPane
}
