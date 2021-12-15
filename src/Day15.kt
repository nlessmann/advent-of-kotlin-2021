import java.io.File
import java.util.PriorityQueue

class Cavern(input: List<String>, grow: Boolean = false) {
    private class Waypoint(val riskLevel: Int) {
        val neighbors = mutableSetOf<Waypoint>()
    }

    private val waypoints: List<Waypoint>
    private val origin: Waypoint
    private val destination: Waypoint

    init {
        var values = input.map { line -> line.trim().map { c -> c.digitToInt() } }

        // Replicate grid?
        if (grow) {
            val valuesX = values.map { it.toMutableList() }
            for (offset in 1..4) {
                for (i in valuesX.indices) {
                    valuesX[i].addAll(
                        values[i].map { (it + offset).let { v -> if (v > 9) v % 9 else v } }
                    )
                }
            }

            val valuesXY = valuesX.toMutableList()
            for (offset in 1..4) {
                for (row in valuesX) {
                    valuesXY.add(
                        row.map { (it + offset).let { v -> if (v > 9) v % 9 else v } }.toMutableList()
                    )
                }
            }

            values = valuesXY
        }

        // Parse grid of risk levels into individual waypoints
        val grid = values.map { row -> row.map { Waypoint(it) } }

        // Add edges to graph
        grid.forEachIndexed { i, row -> row.forEachIndexed { j, waypoint ->
            for (idx in listOf(i - 1 to j, i + 1 to j, i to j - 1, i to j + 1)) {
                if (idx.first in grid.indices && idx.second in row.indices) {
                    waypoint.neighbors.add(grid[idx.first][idx.second])
                }
            }
        } }

        // Turn into flat list and mark special nodes
        waypoints = grid.flatten()
        origin = waypoints.first()
        destination = waypoints.last()
    }

    fun findLeastRiskyPath(): Int? {
        // Use priority queue to traverse the graph
        val traversed = mutableSetOf<Waypoint>()
        val risks = mutableMapOf<Waypoint, Int>()
        val queue = PriorityQueue<Waypoint>(compareBy { risks[it] ?: Int.MAX_VALUE })

        risks[origin] = 0
        queue.add(origin)

        while (queue.isNotEmpty()) {
            // Remove waypoint with the lowest total risk level
            val waypoint = queue.remove()
            val totalRisk = risks[waypoint]
                ?: throw IllegalStateException("Waypoint in queue but not in risks table")

            // If we have reached the destination, we can stop
            traversed.add(waypoint)
            if (waypoint == destination) {
                return totalRisk
            }

            waypoint.neighbors.filter { it !in traversed }.forEach {
                val risk = totalRisk + it.riskLevel
                if (risk < (risks[it] ?: Int.MAX_VALUE)) {
                    risks[it] = risk

                    // Re-add element to update sorting in the queue
                    queue.remove(it)
                    queue.add(it)
                }
            }
        }

        // Did not find a path to the destination
        return null
    }
}

fun main() {
    val input = File("inputs", "day15.txt").readLines()

    val smallCavern = Cavern(input)
    println(smallCavern.findLeastRiskyPath())

    val largeCavern = Cavern(input, grow = true)
    println(largeCavern.findLeastRiskyPath())
}
