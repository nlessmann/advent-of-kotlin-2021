import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class CrabSwarm(initialPositions: List<Int>) {
    private val positions = initialPositions.sorted()

    fun computeMinimalFuelCost(): Int {
        // We are looking for the geometric median as that minimizes the sum of distances
        val medianIndex = positions.size / 2
        val medianPosition =
            if (positions.size % 2 == 0) {
                ((positions[medianIndex] + positions[medianIndex - 1]) / 2.0).roundToInt()
            } else {
                positions[medianIndex]
            }

        return positions.sumOf { position -> (medianPosition - position).absoluteValue }
    }

    fun computeMinimalWeightedFuelCost(): Int {
        // We are looking for a position near the centroid as that minimizes the sum of
        // squared distances, which is about what we need
        val centroid = positions.average().roundToInt()
        val candidatePositions = listOf(centroid - 1, centroid, centroid + 1)

        return candidatePositions.minOf { targetPosition ->
            positions.sumOf { position ->
                (targetPosition - position).absoluteValue.let { n ->
                    n * (n + 1) / 2
                }
            }
        }
    }
}

fun main() {
    val positions = File("inputs", "day7.txt").readText().split(",").map { it.toInt() }
    val swarm = CrabSwarm(positions)
    println("Solution 1: ${swarm.computeMinimalFuelCost()}")
    println("Solution 2: ${swarm.computeMinimalWeightedFuelCost()}")
}
