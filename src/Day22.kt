import java.io.File

class Zone(x: IntRange, y: IntRange, z: IntRange, private val sign: Int) {
    private val corners = listOf(x, y, z)

    fun isPositive(): Boolean {
        return sign > 0
    }

    fun isInitStep(): Boolean {
        // Initialization steps are zones entirely within -50 to 50
        return corners.all { it.all { i -> i in -50..50 } }
    }

    fun signedVolume(): Long {
        // Volume of the zone combined with its sign
        val volume = corners.map { it.last - it.first + 1L }.reduce { a, b -> a * b }
        return sign * volume
    }

    fun intersection(other: Zone): Zone? {
        val corners = corners.zip(other.corners).map { (a, b) ->
            maxOf(a.first, b.first)..minOf(a.last, b.last)
        }
        if (corners.any { it.isEmpty() }) {
            return null
        }

        return Zone(corners[0], corners[1], corners[2], -other.sign)
    }
}

fun rebootReactor(instructions: List<Zone>): Long {
    val zones = mutableListOf<Zone>()
    for (zone in instructions) {
        zones.addAll(buildList {
            // Add positive zones only
            if (zone.isPositive()) {
                add(zone)
            }

            // Add intersections with prior zones
            zones.mapNotNull { zone.intersection(it) }.forEach { add(it) }
        })
    }

    return zones.sumOf { it.signedVolume() }
}

fun main() {
    val instructions = File("inputs", "day22.txt").readLines().map { line ->
        val sign = if (line.startsWith("on")) 1 else -1
        val c = Regex("[0-9-]+").findAll(line).map { it.value.toInt() }.toList()
        Zone(c[0]..c[1], c[2]..c[3], c[4]..c[5], sign)
    }

    println("Solution 1: ${rebootReactor(instructions.filter { it.isInitStep() })}")
    println("Solution 2: ${rebootReactor(instructions)}")
}
