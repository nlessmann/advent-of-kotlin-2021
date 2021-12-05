import java.io.File
import kotlin.math.sign

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

class Line(coordinates: List<Int>) {
    val origin = Point(coordinates[0], coordinates[1])
    val destination = Point(coordinates[2], coordinates[3])

    val diagonal: Boolean
        get() = origin.x != destination.x && origin.y != destination.y

    val step: Point
        get() = Point((destination.x - origin.x).sign, (destination.y - origin.y).sign)
}

class Canvas(width: Int, height: Int) {
    private val grid = MutableList(width) { MutableList(height) { 0 } }

    fun draw(lines: Iterable<Line>, ignoreDiagonalLines: Boolean = false) {
        for (line in lines) {
            if (ignoreDiagonalLines && line.diagonal) {
                continue
            }

            grid[line.origin.x][line.origin.y] += 1

            var position = line.origin.copy()
            while (position != line.destination) {
                position += line.step
                grid[position.x][position.y] += 1
            }
        }
    }

    fun overlap(): Int {
        return grid.sumOf { col -> col.count { it >= 2 } }
    }

    fun clear() {
        grid.forEach { it.fill(0) }
    }
}

fun main() {
    val lines = File("inputs", "day5.txt").readLines()
    val integers = Regex("[0-9]+")
    val coordinates = lines.map {
        Line(integers.findAll(it).map { match -> match.value.toInt() }.toList())
    }

    val width = coordinates.maxOf { line -> maxOf(line.origin.x, line.destination.x) } + 1
    val height = coordinates.maxOf { line -> maxOf(line.origin.y, line.destination.y) } + 1
    val canvas = Canvas(width, height)

    canvas.draw(coordinates, ignoreDiagonalLines = true)
    println("Solution 1: ${canvas.overlap()}")

    canvas.clear()
    canvas.draw(coordinates, ignoreDiagonalLines = false)
    println("Solution 2: ${canvas.overlap()}")
}
