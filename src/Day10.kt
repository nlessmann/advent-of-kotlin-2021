import java.io.File
import kotlin.IllegalArgumentException

class NavigationInstruction(line: String) {
    companion object {
        val closingCharacter = Regex("[)\\]}>]")
    }

    val corruptionScore: Int
    val completionScore: Long

    val corrupted: Boolean
        get() = corruptionScore > 0

    val incomplete: Boolean
        get() = completionScore > 0

    init {
        // Remove chunks until no more chunks can be removed, from the remaining
        // characters we can tell whether the line is corrupted or incomplete
        var previousVersion: String
        var reducedLine = line

        do {
            previousVersion = reducedLine
            reducedLine = previousVersion
                .replace("()", "")
                .replace("[]", "")
                .replace("{}", "")
                .replace("<>", "")
        } while (reducedLine != previousVersion && reducedLine.isNotBlank())

        // Compute corruption score based on first remaining closing character
        // (if there is none, the result will be null and the score 0)
        corruptionScore = when (closingCharacter.find(reducedLine)?.value) {
            ")" -> 3
            "]" -> 57
            "}" -> 1197
            ">" -> 25137
            else -> 0
        }

        completionScore = if (reducedLine.isBlank() || corrupted) {
            // Line was either completely fine, or corrupted
            0
        } else {
            // Line is not corrupted, but incomplete - missing closing characters are
            // the opening characters in reverse order, use that to compute the score
            reducedLine.reversed().fold(0) { score, openingCharacter ->
                score * 5 + when (openingCharacter) {
                    '(' -> 1
                    '[' -> 2
                    '{' -> 3
                    '<' -> 4
                    else -> throw IllegalArgumentException("Line contains illegal characters")
                }
            }
        }
    }
}

fun main() {
    val instructions  = File("inputs", "day10.txt").readLines().map { NavigationInstruction(it) }

    val syntaxErrorScore = instructions.sumOf { it.corruptionScore }
    println("Solution 1: $syntaxErrorScore")

    val completionScores = instructions.filter { !it.corrupted && it.incomplete }.map { it.completionScore }.sorted()
    val medianIndex = completionScores.size / 2
    println("Solution 2: ${completionScores[medianIndex]}")
}
