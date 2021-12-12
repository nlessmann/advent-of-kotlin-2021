import java.io.File

class Cave(val name: String) {
    private val connections = mutableSetOf<Cave>()

    val neighbors: List<Cave>
        get() = connections.sortedBy { it.name }

    fun isStart(): Boolean {
        return name == "start"
    }

    fun isEnd(): Boolean {
        return name == "end"
    }

    fun isBig(): Boolean {
        return name.all { it.isUpperCase() }
    }

    fun isSmall(): Boolean {
        return !isStart() && !isEnd() && !isBig()
    }

    fun findPathsToEnd(allowSmallCaveRevisit: Boolean = false): List<List<Cave>> {
        return findPathsToEnd(mutableListOf(), allowSmallCaveRevisit)
    }

    private fun findPathsToEnd(
        path: MutableList<Cave>,
        allowSmallCaveRevisit: Boolean
    ): List<List<Cave>> {
        // Add cave to path and check whether we've reached the end
        path.add(this)
        if (isEnd()) {
            return listOf(path)
        }

        // Determine whether we can still revisit a small cave
        val canStillRevisit = if (allowSmallCaveRevisit) {
            path.filter { it.isSmall() }.groupingBy { it }.eachCount().values.all { it == 1 }
        } else {
            false
        }

        // Send expeditions into all unvisited neighboring caves
        val paths = mutableListOf<List<Cave>>()
        neighbors.forEach { cave ->
            val previousVisits = path.count { it == cave }
            if (cave.isBig() || previousVisits == 0 || (canStillRevisit && cave.isSmall() && previousVisits < 2)) {
                paths.addAll(
                    cave.findPathsToEnd(path.toMutableList(), allowSmallCaveRevisit)
                )
            }
        }
        return paths
    }

    companion object {
        fun graphFromFile(filename: String): Cave {
            val edgeDefinitions = File(filename).readLines().map {
                it.trim().let { s ->
                    Pair(s.substringBefore("-"), s.substringAfter("-"))
                }
            }

            // Construct all vertices
            val caves = mutableMapOf<String, Cave>()
            val edges = edgeDefinitions.map {
                Pair(
                    caves.getOrPut(it.first) { Cave(it.first) },
                    caves.getOrPut(it.second) { Cave(it.second) }
                )
            }

            // Add edges
            edges.forEach {
                val (caveA, caveB) = it
                caveA.connections.add(caveB)
                caveB.connections.add(caveA)
            }

            // Return start vertex
            return caves.values.single { it.isStart() }
        }
    }
}

fun main() {
    val startCave = Cave.graphFromFile("inputs/day12.txt")

    val paths1 = startCave.findPathsToEnd()
    println("Solution 1: ${paths1.size}")

    val paths2 = startCave.findPathsToEnd(allowSmallCaveRevisit = true)
    println("Solution 2: ${paths2.size}")
}
