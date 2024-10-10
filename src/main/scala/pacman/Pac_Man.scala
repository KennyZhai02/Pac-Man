package pacman

class Pac_Man(var x: Int, var y: Int) {
  var score: Int = 0
  var lives: Int = 3

  def move(dx: Int, dy: Int): Unit = {
    x += dx
    y += dy
  }

  def eatDot(): Unit = {
    score += 10
  }

  def eatPowerPellet(): Unit = {
    score += 50
  }

  def loseLife(): Unit = {
    lives -= 1
  }

  def reset(startX: Int, startY: Int): Unit = {
    x = startX
    y = startY
  }
}