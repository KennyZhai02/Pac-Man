// Ghost.scala
package pacman

import scalafx.scene.paint.Color
import scala.util.Random

abstract class Ghost(var x: Int, var y: Int, val name: String, var speed: Int = 10) {
  val color: Color
  protected val random = new Random()

  def move(dx: Int, dy: Int): Unit = {
    x += dx
    y += dy
  }

  def decideMove(pacManX: Int, pacManY: Int, map: GameMap): (Int, Int)

  protected def randomMove(possibleMoves: List[(Int, Int)]): (Int, Int) = {
    possibleMoves(random.nextInt(possibleMoves.size))
  }

  protected def filterValidMoves(map: GameMap): List[(Int, Int)] = {
    List((-1, 0), (1, 0), (0, -1), (0, 1))
      .filter { case (dx, dy) => map.isValidMove(x + dx, y + dy) }
  }
}

class Blinky(x: Int, y: Int) extends Ghost(x, y, "Blinky") {
  val color: Color = Color.Red

  def decideMove(pacManX: Int, pacManY: Int, map: GameMap): (Int, Int) = {
    if (random.nextDouble() < 1.0 / speed) {
      val possibleMoves = filterValidMoves(map)
      possibleMoves.minBy { case (dx, dy) =>
        math.sqrt(math.pow(pacManX - (x + dx), 2) + math.pow(pacManY - (y + dy), 2))
      }
    } else {
      (0, 0)
    }
  }
}

class Pinky(x: Int, y: Int) extends Ghost(x, y, "Pinky") {
  val color: Color = Color.Pink

  def decideMove(pacManX: Int, pacManY: Int, map: GameMap): (Int, Int) = {
    if (random.nextDouble() < 1.0 / speed) {
      val possibleMoves = filterValidMoves(map)
      val targetX = pacManX + 4
      val targetY = pacManY + 4
      possibleMoves.minBy { case (dx, dy) =>
        math.sqrt(math.pow(targetX - (x + dx), 2) + math.pow(targetY - (y + dy), 2))
      }
    } else {
      (0, 0)
    }
  }
}

class Inky(x: Int, y: Int) extends Ghost(x, y, "Inky") {
  val color: Color = Color.Cyan

  def decideMove(pacManX: Int, pacManY: Int, map: GameMap): (Int, Int) = {
    if (random.nextDouble() < 1.0 / speed) {
      val possibleMoves = filterValidMoves(map)
      if (random.nextDouble() < 0.7) {
        possibleMoves.minBy { case (dx, dy) =>
          math.sqrt(math.pow(pacManX - (x + dx), 2) + math.pow(pacManY - (y + dy), 2))
        }
      } else {
        randomMove(possibleMoves)
      }
    } else {
      (0, 0)
    }
  }
}

class Clyde(x: Int, y: Int) extends Ghost(x, y, "Clyde") {
  val color: Color = Color.Orange

  def decideMove(pacManX: Int, pacManY: Int, map: GameMap): (Int, Int) = {
    if (random.nextDouble() < 1.0 / speed) {
      randomMove(filterValidMoves(map))
    } else {
      (0, 0)
    }
  }
}