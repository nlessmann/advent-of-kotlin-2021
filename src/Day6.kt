import java.io.File

class LanternfishSchool(initialState: List<Long>, private val reproductionTime: Int = 7) {
    var age: Int = 0
        private set

    private var state: List<Long> = List(reproductionTime + 2) { index ->
        initialState.count { it == index.toLong() }.toLong()
    }

    val size: Long
        get() = state.sum()

    fun nextDay() {
        val newState = MutableList(state.size) { 0L }
        state.forEachIndexed { index, n ->
            if (index == 0) {
                newState[reproductionTime - 1] += n
                newState[reproductionTime + 1] += n
            } else if (index > 0) {
                newState[index - 1] += n
            }
        }
        state = newState.toList()
        age++
    }
}

fun main() {
    val values = File("inputs", "day6.txt").readLines().first().trim().split(',').map { it.toLong() }
    val school = LanternfishSchool(values)

    while (school.age < 80) {
        school.nextDay()
    }
    println("Solution 1: ${school.size}")

    while (school.age < 256) {
        school.nextDay()
    }
    println("Solution 2: ${school.size}")
}
