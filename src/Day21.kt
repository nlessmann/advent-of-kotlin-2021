import java.io.File

class Player(initialPosition: Int) {
    var position = initialPosition
        private set

    var points = 0
        private set

    constructor(player: Player) : this(player.position) {
        points = player.points
    }

    fun move(steps: Int) {
        position = (position + steps - 1) % 10 + 1
        points += position
    }
}

interface Dice {
    // Compute a score from two initial positions a and b
    fun roll(a: Int, b: Int): Long
}

class DeterministicDice : Dice {
    private var throws = 0

    override fun roll(a: Int, b: Int): Long {
        return roll(Player(a), Player(b))
    }

    private fun roll(a: Player, b: Player): Long {
        // Next three throws are t+1 + t+2 + t+3 = 3t+6
        a.move(3 * throws + 6)
        throws += 3

        return if (a.points >= 1000) {
            b.points.toLong() * throws
        } else {
            roll(b, a)
        }
    }
}

class DiracDice : Dice {
    private class WinCounter(var a: Long = 0, var b: Long = 0) {
        fun max(): Long {
            return maxOf(a, b)
        }
    }

    override fun roll(a: Int, b: Int): Long {
        return roll(Player(a), Player(b)).max()
    }

    private fun roll(a: Player, b: Player): WinCounter {
        val wins = WinCounter(0, 0)

        for (outcome in outcomes) {
            val player = Player(a)
            player.move(outcome.sum)

            if (player.points >= 21) {
                wins.a += outcome.frequency
            } else {
                // Active player did not win yet, so make other player the active
                // player and continue with the next set of throws
                val swappedWins = roll(b, player)
                wins.a += outcome.frequency * swappedWins.b
                wins.b += outcome.frequency * swappedWins.a
            }
        }

        return wins
    }

    companion object {
        // Maintain a list of all possible outcomes of rolling three Dirac dice
        // and how often these occur (since we can treat them the same)
        private data class Outcome(val sum: Int, val frequency: Int)

        private val outcomes = listOf(
            Outcome(3, 1),
            Outcome(4, 3),
            Outcome(5, 6),
            Outcome(6, 7),
            Outcome(7, 6),
            Outcome(8, 3),
            Outcome(9, 1),
        )
    }
}

fun main() {
    val input = File("inputs", "day21.txt").readLines()
    val positions = input.map { it.substringAfter(':').trim().toInt() }

    val dice = listOf(DeterministicDice(), DiracDice())
    dice.forEachIndexed { i, die ->
        val score = die.roll(positions[0], positions[1])
        println("Solution ${i + 1}: $score")
    }
}
