import java.io.File
import java.util.PriorityQueue
import kotlin.math.absoluteValue

data class Room(val owner: Char, val content: List<Char>) {
    fun isComplete(): Boolean {
        // All spots are occupied by the target amphipod type
        return content.all { it == owner }
    }

    fun hasStrangers(): Boolean {
        // Are there amphipods in this room that are not supposed to be here?
        return content.any { it != owner && it != '.' }
    }

    fun first(): IndexedValue<Char> {
        // Top-most amphipod in the room
        return content.withIndex().first { it.value != '.' }
    }

    fun countFreeSlots(): Int {
        // Number of amphipods that can still fit into this room
        return content.count { it == '.' }
    }

    fun push(): Room {
        // Add a new amphipod of the owner-type to the room
        val newContent = content.toMutableList()
        newContent[newContent.lastIndexOf('.')] = owner
        return Room(owner, newContent)
    }

    fun pop(): Room {
        // Remove the first amphipod in the room
        val newContent = content.toMutableList()
        newContent[first().index] = '.'
        return Room(owner, newContent)
    }
}

data class Burrow(val hallway: List<Char>, val rooms: Map<Int, Room>) {
    private val destinations = buildMap {
        rooms.forEach { (x, room) -> put(room.owner, x) }
    }

    private fun isClearHallwayPath(from: Int, to: Int): Boolean {
        val indices = if (from < to) {
            (from + 1) until (to + 1)
        } else {
            to until from
        }
        return hallway.slice(indices).all { !it.isLetter() }
    }

    private class Move(val burrow: Burrow, val cost: Int) {
        constructor(burrow: Burrow, steps: Int, amphipod: Char) : this(burrow, steps * costs.getValue(amphipod))

        companion object {
            private val costs = mapOf('A' to 1, 'B' to 10, 'C' to 100, 'D' to 1000)
        }
    }

    private fun possibleMoves(): List<Move> {
        return buildList {
            // Room to Hallway
            for (x in destinations.values) {
                val room = rooms.getValue(x)
                if (!room.hasStrangers()) continue

                // First amphipod in a room with a stranger
                val (y, amphipod) = room.first()

                // Add all possible moves to the hallway to the list
                hallway.forEachIndexed { xx, c ->
                    if (c == '.' && isClearHallwayPath(x, xx)) {
                        val steps = y + 1 + (x - xx).absoluteValue

                        val newHallway = hallway.toMutableList()
                        newHallway[xx] = amphipod
                        val newRooms = rooms.toMutableMap()
                        newRooms[x] = room.pop()
                        val burrow = Burrow(newHallway, newRooms)

                        add(Move(burrow, steps, amphipod))
                    }
                }
            }

            // Hallway to room
            hallway.withIndex().filter { it.value.isLetter() }.forEach { (x, amphipod) ->
                val xx = destinations.getValue(amphipod)
                val room = rooms.getValue(xx)
                if (!room.hasStrangers() && isClearHallwayPath(x, xx)) {
                    val steps = room.countFreeSlots() + (x - xx).absoluteValue

                    val newHallway = hallway.toMutableList()
                    newHallway[x] = '.'
                    val newRooms = rooms.toMutableMap()
                    newRooms[xx] = room.push()
                    val burrow = Burrow(newHallway, newRooms)

                    add(Move(burrow, steps, amphipod))
                }
            }
        }
    }

    private fun isOrganized(): Boolean {
        return rooms.values.all { it.isComplete() }
    }

    fun organize(): Int? {
        // Use priority queue to traverse the graph of possible moves
        val reached = mutableSetOf<Burrow>()
        val costs = mutableMapOf<Burrow, Int>()
        val queue = PriorityQueue<Burrow>(compareBy { costs.getValue(it) })

        costs[this] = 0
        queue.add(this)

        while (queue.isNotEmpty()) {
            // Remove move with the lowest total cost
            val burrow = queue.remove()
            val totalCost = costs.getValue(burrow)

            // For better efficiency, we allow duplicates in the priority queue,
            // so this configuration might have been visited before at lower cost
            if (burrow in reached) continue
            reached.add(burrow)

            // Add possible moves from this state to the queue
            burrow.possibleMoves().filter { it.burrow !in reached }.forEach {
                val cost = totalCost + it.cost
                if (cost < (costs[it.burrow] ?: Int.MAX_VALUE)) {
                    costs[it.burrow] = cost
                    queue.add(it.burrow)
                }
            }
        }

        return costs.filter { (burrow, _) -> burrow.isOrganized() }.minOfOrNull { (_, cost) -> cost }
    }

    companion object {
        fun fromFile(filename: String): Burrow {
            val input = File(filename).readLines()

            // Parse hallway into available and unavailable (above rooms) spots
            val hallway = input[1].mapIndexedNotNull { i, c ->
                when {
                    c == '#' -> null
                    input[2][i] != '#' -> '-'
                    else -> '.'
                }
            }

            // Parse rooms from A to D
            var owner = 'A'
            val rooms = buildMap {
                for (x in hallway.indices) {
                    if (hallway[x] == '-') {
                        val content = input.subList(2, input.size - 1).map { it[x + 1] }
                        put(x, Room(owner++, content))
                    }
                }
            }

            return Burrow(hallway, rooms)
        }
    }
}

fun main() {
    val cost1 = Burrow.fromFile("inputs/day23_1.txt").organize()
    println("Solution 1: $cost1")

    val cost2 = Burrow.fromFile("inputs/day23_2.txt").organize()
    println("Solution 2: $cost2")
}
