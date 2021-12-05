import java.io.File

typealias Word = List<Int>

fun binaryToDecimal(bits: Word): Int {
    return bits.joinToString("").toInt(2)
}

fun mostCommonBit(words: List<Word>, index: Int): Int {
    val ones: Int = words.count { it[index] == 1 }
    val onesRatio = ones.toDouble() / words.size
    return if (onesRatio >= 0.5) 1 else 0
}

fun filterWords(words: List<Word>, filter: (Int, Int) -> Boolean): Word {
    val bits = words.first().size
    var remainingWords = words.toList()
    for (i in 0 until bits) {
        val bit = mostCommonBit(remainingWords, i)
        remainingWords = remainingWords.filter { filter(it[i], bit) }
        if (remainingWords.size <= 1) {
            break
        }
    }

    return remainingWords.first()
}

fun main() {
    val lines = File("inputs", "day3.txt").readLines()
    val report: List<Word> = lines.map { it.map { c -> Character.getNumericValue(c) } }

    // Number of bits per measurement
    val bits = report.first().size

    // Compute gamma rate
    val gammaRate = mutableListOf<Int>()
    for (i in 0 until bits) {
        gammaRate.add(mostCommonBit(report, i))
    }

    // Compute epsilon rate
    val epsilonRate = gammaRate.map { if (it == 1) 0 else 1 }

    // Compute power consumption
    val powerConsumption = binaryToDecimal(gammaRate) * binaryToDecimal(epsilonRate)
    println("Power consumption: $powerConsumption")

    // Compute life support rating
    val oxygenGeneratorRating = filterWords(report) { v, r -> v == r }
    val co2ScrubberRating = filterWords(report) { v, r -> v != r }
    val lifeSupportRating = binaryToDecimal(oxygenGeneratorRating) * binaryToDecimal(co2ScrubberRating)
    println("Life support rating: $lifeSupportRating")
}
