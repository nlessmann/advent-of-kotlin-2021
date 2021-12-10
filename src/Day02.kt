import java.io.File

open class Submarine {
    protected var horizontalPosition: Int = 0
    protected var depth: Int = 0

    val position: Int get() = horizontalPosition * depth

    open fun move(direction: String, distance: Int) {
        when (direction) {
            "forward" -> horizontalPosition += distance
            "up" -> depth -= distance
            "down" -> depth += distance
        }
    }

    fun followCourse(commands: List<String>) {
        val course = commands.map { it.trim().split(' ') }
        for ((direction, distance) in course) {
            move(direction, distance.toInt())
        }
    }
}

class Submarine2 : Submarine() {
    private var aim: Int = 0

    override fun move(direction: String, distance: Int) {
        when (direction) {
            "forward" -> {
                horizontalPosition += distance
                depth += aim * distance
            }
            "up" -> aim -= distance
            "down" -> aim += distance
        }
    }
}

fun main() {
    val commands = File("inputs", "day02.txt").readLines()
    val submarines = listOf(Submarine(), Submarine2())
    submarines.forEachIndexed { index, submarine ->
        submarine.followCourse(commands)
        println("Solution ${index + 1}: ${submarine.position}")
    }
}
