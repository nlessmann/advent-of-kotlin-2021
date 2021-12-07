import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class CrabSwarm(initialPositions: List<Int>) {
    private val positions = initialPositions.sorted()

    fun computeMinimalFuelCost(): Int {
        // We are looking for the geometric median as that minimizes the sum of distances
        val medianIndex = positions.size / 2

        var medianPosition = positions[medianIndex]
        if (positions.size % 2 == 0) {
            val realMedian = (medianPosition + positions[medianIndex - 1]).toDouble() / 2
            medianPosition = realMedian.roundToInt()
        }

        return positions.sumOf { position -> (medianPosition - position).absoluteValue }
    }

    fun computeWeightedMinimalFuelCost(searchWindow: Int = 10): Int {
        // We are looking for a position near the centroid as that minimizes the sum of
        // squared distances, which is about what we need
        val centroid = positions.average().roundToInt()
        val candidatePositions = centroid - searchWindow..centroid + searchWindow

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
    println("Solution 2: ${swarm.computeWeightedMinimalFuelCost()}")
}
