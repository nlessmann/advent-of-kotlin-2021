import java.io.File

class Polymer(filename: String) {
    private val elements: List<Char>
    private val pairInsertionRules: Map<Pair<Char, Char>, Char>

    init {
        val input = File(filename).readLines()
        elements = input[0].trim().toList()
        pairInsertionRules = input.subList(2, input.size).associateBy(
            keySelector = { it[0] to it[1] },
            valueTransform = { it[6] }
        )
    }

    private data class Evolution(val chars: Pair<Char, Char>, val steps: Int)
    private val lut: MutableMap<Evolution, Map<Char, Long>> = mutableMapOf()

    fun evolve(steps: Int): Long {
        // Count initial number of characters
        val counts = elements.associateWith { c ->
            elements.count { it == c }.toLong()
        }.toMutableMap()

        // Evolve pairs of 2 characters
        elements.windowed(2).forEach {
           evolvePair(it[0] to it[1], steps).forEach { (c, n) ->
               counts[c] = (counts[c] ?: 0) + n
           }
        }

        // Compute score based on most and least common elements
        return counts.maxOf { it.value } - counts.minOf { it.value }
    }

    private fun evolvePair(chars: Pair<Char, Char>, steps: Int): Map<Char, Long> {
        // Two characters and remaining evolution steps are a unique identifier
        val evolution = Evolution(chars, steps)

        if (evolution !in lut) {
            // Look up which additional character we need to insert
            val additionalChar = pairInsertionRules[chars]
                ?: throw IllegalArgumentException("Illegal character")

            // Initialize counter with the new character
            val counts = mutableMapOf(additionalChar to 1L)

            // If there are more steps to be done, create two new pairs and continue
            if (steps > 1) {
                for (pair in listOf(chars.first to additionalChar, additionalChar to chars.second)) {
                    evolvePair(pair, steps - 1).forEach { (c, n) ->
                        counts[c] = (counts[c] ?: 0) + n
                    }
                }
            }

            lut[evolution] = counts
        }

        return lut[evolution]!!
    }
}

fun main() {
    val polymer = Polymer("inputs/day14.txt")
    println("Solution 1: ${polymer.evolve(10)}")
    println("Solution 2: ${polymer.evolve(40)}")
}
