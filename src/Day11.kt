import java.io.File

class DumboOctopus(initialEnergyLevel: Int) {
    private var energyLevel = initialEnergyLevel

    var flashes = 0
        private set

    var flashed = false
        private set

    private val neighbors = mutableSetOf<DumboOctopus>()

    fun increaseEnergyLevel() {
        if (flashed) return

        energyLevel++
        if (energyLevel > 9) {
            flashes++
            flashed = true
            neighbors.forEach { it.increaseEnergyLevel() }
        }
    }

    fun reset() {
        if (flashed) {
            energyLevel = 0
            flashed = false
        }
    }

    companion object {
        private data class Offset(val x: Int, val y: Int)

        private val neighborOffsets = listOf(
            Offset(-1, -1), Offset(-1, 0), Offset(-1, 1),
            Offset(0, -1), Offset(0, 1),
            Offset(1, -1), Offset(1, 0), Offset(1, 1)
        )

        fun gridFromFile(filename: String): List<DumboOctopus> {
            val input = File(filename).readLines()
            val octopuses = input.map { it.map { c -> DumboOctopus(c.digitToInt()) } }
            octopuses.forEachIndexed { i, o -> o.forEachIndexed { j, octopus ->
                neighborOffsets.forEach {
                    val x = i + it.x
                    val y = j + it.y

                    if (x in octopuses.indices && y in o.indices) {
                        octopus.neighbors.add(octopuses[x][y])
                    }
                }
            } }
            return octopuses.flatten()
        }
    }
}

fun main() {
    val octopuses = DumboOctopus.gridFromFile("inputs/day11.txt")

    var step = 0
    var allFlashed = false
    while (step < 100 || !allFlashed) {
        // Increase energy level of all octopuses
        octopuses.forEach { it.increaseEnergyLevel() }
        step++

        // Print total number of flashes after 100 steps
        if (step == 100) {
            val flashes = octopuses.sumOf { it.flashes }
            println("Solution 1: $flashes")
        }

        // Check whether all octopuses flashed in this round
        if (!allFlashed && octopuses.all { it.flashed }) {
            allFlashed = true
            println("Solution 2: $step")
        }

        octopuses.forEach { it.reset() }
    }
}
