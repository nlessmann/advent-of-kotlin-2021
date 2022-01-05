import java.io.File

class Cuboid(val x: IntRange, val y: IntRange, val z: IntRange, val sign: Int) {
    private val ranges = listOf(x, y, z)

    fun isInitStep(): Boolean {
        return ranges.all { it.all { i -> i in -50..50 } }
    }

    fun isEmpty(): Boolean {
        return x.isEmpty() || y.isEmpty() || z.isEmpty()
    }

    fun signedVolume(): Long {
        val volume = ranges.map { it.last - it.first + 1L }.reduce { a, b -> a * b }
        return sign * volume
    }

    fun intersection(other: Cuboid): Cuboid {
        val corners = ranges.zip(other.ranges).map { (a, b) ->
            maxOf(a.first, b.first)..minOf(a.last, b.last)
        }
        return Cuboid(corners[0], corners[1], corners[2], -other.sign)
    }
}

fun rebootReactor(instructions: List<Cuboid>): Long {
    val zones = mutableListOf<Cuboid>()
    for (cube in instructions) {
        zones.addAll(buildList {
            if (cube.sign > 0) {
                add(cube)
            }

            val intersections = zones.map { cube.intersection(it) }
            intersections.filter { !it.isEmpty() }.forEach { add(it) }
        })
    }

    return zones.sumOf { it.signedVolume() }
}

fun main() {
    val instructions = File("inputs", "day22.txt").readLines().map { line ->
        val sign = if (line.startsWith("on")) 1 else -1
        val n = Regex("[0-9-]+").findAll(line).map { it.value.toInt() }.toList()
        Cuboid(n[0]..n[1], n[2]..n[3], n[4]..n[5], sign)
    }

    println("Solution 1: ${rebootReactor(instructions.filter { it.isInitStep() })}")
    println("Solution 2: ${rebootReactor(instructions)}")
}
