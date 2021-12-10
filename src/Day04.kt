import java.io.File

class BingoBoard(private val numbers: List<List<Int>>) {
    private val drawn = MutableList(numbers.size) { MutableList(numbers.size) { false } }

    var score: Int? = null
        private set

    val won: Boolean
        get() = score != null

    fun mark(number: Int) {
        numbers.forEachIndexed { i, col -> col.forEachIndexed { j, n ->
            if (n == number) {
                drawn[i][j] = true
            }
        } }

        // Bingo?
        if (drawn.any { col -> col.all { it } }) {
            computeScore(number)
        } else {
            for (i in 0 until drawn.size) {
                if (drawn.all { it[i] }) {
                    computeScore(number)
                    break
                }
            }
        }
    }

    private fun computeScore(draw: Int) {
        val unmarkedNumbers = numbers.foldIndexed(0) { i, total, col ->
            total + col.foldIndexed(0) { j, sum, value ->
                if (drawn[i][j]) sum else sum + value
            }
        }
        score = unmarkedNumbers * draw
    }
}

fun main() {
    val lines = File("inputs", "day04.txt").readLines()

    // Parse list of drawn numbers in the first line of the file
    val draws = lines.first().trim().split(",").map { it.toInt() }

    // Use remaining lines to construct bingo boards
    val linesWithBoards = lines.drop(1).filter { it.isNotBlank() }
    val integers = Regex("[0-9]+")
    val boardNumbers = linesWithBoards.map { integers.findAll(it).map { m -> m.value.toInt() }.toList() }
    val boardSize = boardNumbers.first().size
    val boards = boardNumbers.chunked(boardSize).map { BingoBoard(it) }

    // Process draws
    var playingBoards = boards
    val winningBoards = mutableListOf<BingoBoard>()

    for (draw in draws) {
        for (board in playingBoards) {
            board.mark(draw)
            if (board.won) {
                winningBoards.add(board)
            }
        }

        playingBoards = playingBoards.filter { !it.won }
        if (playingBoards.isEmpty()) break
    }

    // Print results
    println("Solution 1: ${winningBoards.first().score}")
    println("Solution 2: ${winningBoards.last().score}")
}
