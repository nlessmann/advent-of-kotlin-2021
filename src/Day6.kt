import java.io.File

class LanternfishSchool(fish: List<Int>, private val reproductionTime: Int = 7, private val childhood: Int = 2) {
    var age: Int = 0
        private set

    // Number of fish per remaining days to reproduction
    private var state: List<Long> = List(reproductionTime + childhood) { index ->
        fish.count { it == index }.toLong()
    }

    val size: Long
        get() = state.sum()

    fun nextDay() {
        // Determine new state by moving fish from n to n - 1 days
        val newState = MutableList(state.size) { 0L }
        state.forEachIndexed { index, n ->
            if (index == 0) {
                newState[reproductionTime - 1] += n
                newState[reproductionTime + childhood - 1] += n
            } else {
                newState[index - 1] += n
            }
        }

        // Replace current state with new state
        state = newState
        age++
    }
}

fun main() {
    val fish = File("inputs", "day6.txt").readText().split(',').map { it.toInt() }
    val school = LanternfishSchool(fish)

    while (school.age < 80) {
        school.nextDay()
    }
    println("Solution 1: ${school.size}")

    while (school.age < 256) {
        school.nextDay()
    }
    println("Solution 2: ${school.size}")
}
