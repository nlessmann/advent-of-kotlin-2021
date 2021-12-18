import java.io.File

class SnailfishNumber(private val values: List<Int>, private val depths: List<Int>) {
    fun magnitude(): Int {
        return if (depths.isEmpty()) {
            // Single value
            values[0]
        } else {
            // Pair of values, split up into two new numbers and
            // use their magnitudes in the formula 3a+2b
            val index = depths.indexOf(1)

            val a = SnailfishNumber(
                values.subList(0, index + 1),
                depths.subList(0, index).map { it - 1 }
            )
            val b = SnailfishNumber(
                values.subList(index + 1, values.size),
                depths.subList(index + 1, depths.size).map { it - 1 }
            )

            3 * a.magnitude() + 2 * b.magnitude()
        }
    }

    private fun explode(): SnailfishNumber {
        val index = depths.indexOfFirst { it > 4 }
        if (index < 0) return this

        // Add values of the offending pair to its neighbors, replace with 0
        val newValues = values.mapIndexedNotNull { i, value ->
            when (i) {
                index - 1 -> value + values[index]
                index -> 0
                index + 1 -> null
                index + 2 -> value + values[index + 1]
                else -> value
            }
        }

        // Remove depth entry of the removed pair
        val newDepths = depths.toMutableList()
        newDepths.removeAt(index)

        return SnailfishNumber(newValues, newDepths).explode()
    }

    private fun split(): SnailfishNumber {
        val index = values.indexOfFirst { it > 9 }
        if (index < 0) return this

        // Replace offending value with a new pair with half of the value
        val newValues = values.toMutableList()
        newValues[index] = values[index] / 2
        newValues.add(index + 1, values[index] - newValues[index])

        // Add depth of the new pair (depth of the value + 1)
        val newDepth = when (index) {
            0 -> depths[0] + 1
            depths.size -> depths[index - 1] + 1
            else -> maxOf(depths[index - 1], depths[index]) + 1
        }
        val newDepths = depths.toMutableList()
        newDepths.add(index, newDepth)

        return SnailfishNumber(newValues, newDepths)
    }

    operator fun plus(other: SnailfishNumber): SnailfishNumber {
        // Values do not change yet, just concatenate them
        val newValues = values.toMutableList()
        newValues.addAll(other.values)

        // Depths increase by one, and a new level 1 layer is added
        val newDepths = depths.map { it + 1 }.toMutableList()
        newDepths.add(1)
        newDepths.addAll(other.depths.map { it + 1 })

        // Explode and split until there is no change anymore
        var newNumber = SnailfishNumber(newValues, newDepths)
        while (true) {
            val reducedNumber = newNumber.explode().split()
            if (reducedNumber == newNumber) break
            newNumber = reducedNumber
        }

        return newNumber
    }

    override fun toString(): String {
       return values.foldIndexed("") { i, s, value ->
            val depth = if (i < depths.size) depths[i] else 0
            val previousDepth = if (i > 0) depths[i - 1] else 0
            s + if (depth > previousDepth) {
                "[".repeat(depth - previousDepth) + value.toString() + ","
            } else {
                value.toString() + "]".repeat(previousDepth - depth) + ","
            }
        }.dropLast(1)
    }

    companion object {
        fun fromString(input: String): SnailfishNumber {
            val values = mutableListOf<Int>()
            val depths = mutableListOf<Int>()

            var depth = 0
            input.trim().forEach { c ->
                when (c) {
                    '[' -> depth++
                    ']' -> depth--
                    ',' -> depths.add(depth)
                    else -> values.add(c.digitToInt())
                }
            }

            return SnailfishNumber(values, depths)
        }
    }
}

fun main() {
    val numbers = File("inputs", "day18.txt").readLines().map { SnailfishNumber.fromString(it) }

    // Sum up all numbers
    val sum = numbers.reduce { a, b -> a + b }
    println("Solution 1: ${sum.magnitude()}")

    // Add each number to a single other number, find maximum amplitude
    val largestMagnitude = numbers.maxOf { n ->
        numbers.filter { it != n }.maxOf { (n + it).magnitude() }
    }
    println("Solution 2: $largestMagnitude")
}
