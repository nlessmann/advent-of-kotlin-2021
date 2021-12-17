import java.io.File

class TargetArea(definition: String) {
    val x: IntRange
    val y: IntRange

    val xVelocity: IntRange
    val yVelocity: IntRange

    init {
        val match = Regex("x=([0-9]+)\\.\\.([0-9]+), y=(-?[0-9]+)\\.\\.(-?[0-9]+)").find(definition)
            ?: throw IllegalArgumentException("Invalid target area definition")
        val values = match.groupValues.drop(1).map { it.toInt() }

        if (values[0] <= 0)
            throw IllegalArgumentException("Target area needs to be to the right of the origin")
        if (values[2] >= 0)
            throw IllegalArgumentException("Target area needs to be below the origin")

        x = values[0]..values[1]
        y = values[2]..values[3]

        xVelocity = 0..x.last
        yVelocity = y.first..-(y.first + 1)
    }

    operator fun contains(probe: Probe): Boolean {
        return (probe.x in x && probe.y in y)
    }
}

data class Probe(var xVelocity: Int, var yVelocity: Int) {
    var x = 0
    var y = 0

    fun move() {
        // The probe's x position increases by its x velocity.
        x += xVelocity

        // The probe's y position increases by its y velocity.
        y += yVelocity

        // Due to drag, the probe's x velocity changes by 1 toward the value 0.
        if (xVelocity > 0) {
            xVelocity--
        }

        // Due to gravity, the probe's y velocity decreases by 1.
        yVelocity--
    }
}

fun main() {
    val input = File("inputs", "day17.txt").readText()
    val targetArea = TargetArea(input)

    // Compute the highest point reached with the maximum y-velocity
    val yMaxVelocity = targetArea.yVelocity.last
    val yMax = (yMaxVelocity * (yMaxVelocity + 1)) / 2
    println("Solution 1: $yMax")

    // Brute-force search for x/y velocity combinations
    var suitableVelocities = 0
    for (xVelocity in targetArea.xVelocity) {
        for (yVelocity in targetArea.yVelocity) {
            val probe = Probe(xVelocity, yVelocity)
            while (probe.x <= targetArea.x.last && probe.y >= targetArea.y.first) {
                if (probe in targetArea) {
                    suitableVelocities++
                    break
                }
                probe.move()
            }
        }
    }
    println("Solution 2: $suitableVelocities")
}
