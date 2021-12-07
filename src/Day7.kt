import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.floor

class CrabSwarm(initialPositions: List<Int>) {
    private val positions = initialPositions.sorted()

    fun computeMinimalFuelCost(): Int {
        // We are looking for the geometric median as that minimizes the sum of distances
        val medianIndex = positions.size / 2

        val medianPositions = mutableListOf(positions[medianIndex])
        if (positions.size % 2 == 0) {
            medianPositions.add(positions[medianIndex - 1])
        }

        return medianPositions.minOf { targetPosition ->
            positions.sumOf { position -> (targetPosition - position).absoluteValue }
        }
    }

    fun computeWeightedMinimalFuelCost(): Int {
        // We are looking for a position near the center of mass (where the sum of
        // weighted distances is minimal)
        val averagePosition = positions.average()
        val averagePositions = listOf(floor(averagePosition), ceil(averagePosition))
        return averagePositions.minOf { targetPosition ->
            positions.sumOf { position ->
                (targetPosition.toInt() - position).absoluteValue.let { n ->
                    n * (n + 1) / 2
                }
            }
        }
    }
}

fun main() {
    val positions = File("inputs", "day7.txt").readText().trim().split(",").map { it.toInt() }
    val swarm = CrabSwarm(positions)
    println("Solution 1: ${swarm.computeMinimalFuelCost()}")
    println("Solution 2: ${swarm.computeWeightedMinimalFuelCost()}")
}
