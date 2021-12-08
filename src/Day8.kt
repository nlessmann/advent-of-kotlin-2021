import java.io.File

class Display(patterns: String) {
    companion object {
        private val segment_expression = Regex("[a-g]+")
    }

    val inputs: List<String>
    val outputs: List<String>

    init {
        val segments = segment_expression.findAll(patterns).map {
            it.value.toCharArray().sorted().joinToString("")
        }.toList()

        inputs = segments.subList(0, 10)
        outputs = segments.subList(10, segments.size)
    }

    fun decode(): Int {
        val lut = MutableList(10) { "" }

        // Some digits have a unique number of segments
        lut[1] = inputs.single { it.length == 2 }
        lut[7] = inputs.single { it.length == 3 }
        lut[4] = inputs.single { it.length == 4 }
        lut[8] = inputs.single { it.length == 7 }

        // The top most segment is the only difference between 1 and 7
        val a = lut[7].single { it !in lut[1] }

        // 3 has 5 segments, including all of 7
        lut[3] = inputs.single {
            it.length == 5 && lut[7].all { c -> c in it }
        }

        // The bottom most segment is 3 minus a and minus 4
        val g = lut[3].single { it != a && it !in lut[4] }

        // The middle segment is 3 minus a, g and 1
        val d = lut[3].single { it != a && it != g && it !in lut[1] }

        // The top left segment is 4 minus d and minus 1
        val b = lut[4].single { it != d && it !in lut[1] }

        // 0 is 8 minus d
        lut[0] = lut[8].filter { it != d }

        // 9 has six segments and all segments in 4 are also in 9 (but not in 6)
        lut[9] = inputs.single {
            it.length == 6 && it != lut[0] && lut[4].all { c -> c in it }
        }

        // 6 has six segments and is not any of the other six segment digits
        lut[6] = inputs.single {
            it.length == 6 && it != lut[0] && it != lut[9]
        }

        // 2 has five digits and does not contain b (while 5 does)
        lut[2] = inputs.single { it.length == 5 && it != lut[3] && b !in it }
        lut[5] = inputs.single { it.length == 5 && b in it }

        // Decode output
        return outputs.map { lut.indexOf(it) }.joinToString("").toInt()
    }
}

fun main() {
    val inputs = File("inputs", "day8.txt").readLines()
    val displays = List(inputs.size) { i -> Display(inputs[i]) }

    val easyDigitSegments = listOf(2, 3, 4, 7)
    val n = displays.sumOf { it.outputs.count { output -> output.length in easyDigitSegments } }
    println("Solution 1: $n")

    val sum = displays.sumOf { it.decode() }
    println("Solution 2: $sum")
}
