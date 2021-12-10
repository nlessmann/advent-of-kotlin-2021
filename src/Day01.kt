import java.io.File

fun countIncreases(values: List<Int>, windowSize: Int): Int {
    return values.windowed(windowSize).count { it.last() > it.first() }
}

fun main() {
    val values = File("inputs", "day01.txt").readLines().map { it.trim().toInt() }

    println("Solution 1: ${countIncreases(values, 2)}")
    println("Solution 2: ${countIncreases(values, 4)}")
}
