import java.io.File

typealias Path = List<Cave>
typealias MutablePath = MutableList<Cave>

class Cave(val name: String) {
    private val connections = mutableSetOf<Cave>()

    fun addConnection(cave: Cave) {
        connections.add(cave)
    }

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

    fun findPathsToEnd(allowSmallCaveRevisit: Boolean = false): List<Path> {
        return findPathsToEnd(mutableListOf(), allowSmallCaveRevisit)
    }

    private fun findPathsToEnd(path: MutablePath, allowSmallCaveRevisit: Boolean): List<Path> {
        // Add cave to path and check whether we've reached the end
        path.add(this)
        if (isEnd()) {
            return listOf(path)
        }

        // Determine whether a small cave has been visited twice already
        val canStillRevisit = if (allowSmallCaveRevisit) {
            path.filter { it.isSmall() }.let { it.size == it.distinct().size }
        } else {
            false
        }

        // Send expeditions into all unvisited neighboring caves
        val paths = mutableListOf<Path>()
        connections.forEach { cave ->
            val previousVisits = path.count { it == cave }
            if (cave.isBig() || previousVisits == 0 || (previousVisits == 1 && cave.isSmall() && canStillRevisit)) {
                paths.addAll(cave.findPathsToEnd(path.toMutableList(), allowSmallCaveRevisit))
            }
        }
        return paths
    }
}

fun caveGraphFromFile(filename: String): Cave {
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
        caveA.addConnection(caveB)
        caveB.addConnection(caveA)
    }

    // Return start vertex
    return caves.values.single { it.isStart() }
}

fun main() {
    val startCave = caveGraphFromFile("inputs/day12.txt")

    val paths1 = startCave.findPathsToEnd()
    println("Solution 1: ${paths1.size}")

    val paths2 = startCave.findPathsToEnd(allowSmallCaveRevisit = true)
    println("Solution 2: ${paths2.size}")
}
