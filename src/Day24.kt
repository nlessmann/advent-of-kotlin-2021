import java.io.File
import kotlin.math.truncate

class ModelNumberFinder(filename: String) {
    private class Instruction(val xAdd: Int, val yAdd: Int) {
        val push = xAdd > 10
    }

    private val instructions: List<Instruction>

    private fun extractDigitFromLine(line: String): Int {
        val match = Regex("[-0-9]+").find(line)!!
        return match.value.toInt()
    }

    init {
        // Split instructions into blocks separated by "inp" commands
        val blocks = mutableListOf<MutableList<String>>()
        File(filename).readLines().forEach {
            if (it.startsWith("inp w")) {
                blocks.add(mutableListOf(it))
            } else {
                blocks.last().add(it)
            }
        }

        // Convert instructions into objects that hold the few values
        // that differ between blocks of instructions
        instructions = blocks.map { block ->
            Instruction(
                extractDigitFromLine(block[5]),
                extractDigitFromLine(block[15])
            )
        }
    }

    private fun completeNumber(pushDigits: String): String? {
        // Take digits for push instructions and find corresponding pop instructions
        val modelNumber = StringBuilder()
        val knownDigits = pushDigits.toMutableList()

        var z = 0
        for (instruction in instructions) {
            if (instruction.push) {
                // Assume x == 1 (i.e. z + xAdd != w) because xAdd is always >= 10
                val w = knownDigits.removeFirst().digitToInt()
                z = 26 * z + w + instruction.yAdd
                modelNumber.append(w)
            } else {
                // Find w that leads to x == 0 (invalid number if that is not a single digit)
                val w = (z % 26) + instruction.xAdd
                if (w !in 1..9) return null

                z = truncate(z / 26.0).toInt()
                modelNumber.append(w)
            }
        }

        return if (z == 0) modelNumber.toString() else null
    }

    private fun firstValidNumber(pushDigits: Sequence<String>): String? {
        // Return first valid model number that can constructed from the half numbers
        return pushDigits.firstNotNullOfOrNull { completeNumber(it) }
    }

    fun findSmallestNumber(): String? {
        return firstValidNumber(sequence {
            var n = 1111111
            do {
                val s = n.toString()
                if ('0' !in s) yield(s)
            } while (n++ < 9999999)
        })
    }

    fun findLargestNumber(): String? {
        return firstValidNumber(sequence {
            var n = 9999999
            do {
                val s = n.toString()
                if ('0' !in s) yield(s)
            } while (n-- > 1111111)
        })
    }
}

fun main() {
    val finder = ModelNumberFinder("inputs/day24.txt")
    println("Solution 1: ${finder.findLargestNumber()}")
    println("Solution 2: ${finder.findSmallestNumber()}")
}
