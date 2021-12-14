import java.io.File

class TransparentPaper(xy: List<String>) {
    private data class Dot(val x: Int, val y: Int)

    private var dots: List<Dot> = xy.map {
        Dot(it.substringBefore(',').toInt(), it.substringAfter(',').toInt())
    }

    private var folds = 0

    fun foldLeft(x: Int) {
        dots = dots.map { dot ->
            if (dot.x > x) {
                Dot(x - (dot.x - x), dot.y)
            } else {
                dot
            }
        }.distinct()
        folds++
    }

    fun foldUp(y: Int) {
        dots = dots.map { dot ->
            if (dot.y > y) {
                Dot(dot.x, y - (dot.y - y))
            } else {
                dot
            }
        }.distinct()
        folds++
    }

    fun display() {
        val width = dots.maxOf { it.x }
        val height = dots.maxOf { it.y }
        for (y in 0..height) {
            println(
                (0..width).map { x ->
                    if (dots.any { it.x == x && it.y == y }) '█' else ' '
                }.joinToString("")
            )
        }
    }

    fun countDots(): Int {
        return dots.size
    }

    fun countFolds(): Int {
        return folds
    }
}

fun main() {
    val input = File("inputs", "day13.txt").readLines()
    val split = input.indexOfFirst { it.isBlank() }

    // Parse upper half of the input into the initial grid of dots
    val paper = TransparentPaper(input.subList(0, split))

    // Parse lower half and fold paper
    val foldExpression = Regex("([xy])=([0-9]+)")
    for (line in input.subList(split + 1, input.size)) {
        val match = foldExpression.find(line) ?: continue

        val coordinate = match.groupValues[2].toInt()
        if (match.groupValues[1] == "x") {
            paper.foldLeft(coordinate)
        } else {
            paper.foldUp(coordinate)
        }

        if (paper.countFolds() == 1) {
            println("Solution 1: ${paper.countDots()}")
            println()
        }
    }

    paper.display()
}
