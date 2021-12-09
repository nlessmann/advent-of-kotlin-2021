import java.io.File

data class Location(val i: Int, val j: Int) {
    fun neighbors(map: List<List<Int>>): List<Location> {
        val neighbors = mutableListOf<Location>()
        if (i > 0) {
            neighbors.add(Location(i - 1, j))
        }
        if (i + 1 < map.size) {
            neighbors.add(Location(i + 1, j))
        }
        if (j > 0) {
            neighbors.add(Location(i, j - 1))
        }
        if (map.size > 1 && j + 1 < map.first().size) {
            neighbors.add(Location(i, j + 1))
        }
        return neighbors
    }
}

fun findLowPoints(heights: List<List<Int>>): List<Location> {
    val lowPoints = mutableListOf<Location>()
    heights.forEachIndexed { i, row ->
        row.forEachIndexed { j, height ->
            val location = Location(i, j)
            val neighbors = location.neighbors(heights)
            if (neighbors.all { heights[it.i][it.j] > height }) {
                lowPoints.add(location)
            }
        }
    }
    return lowPoints
}

fun measureBasins(heights: List<List<Int>>, seedPoints: List<Location>): List<Int> {
    // Mutable copy of the height map, using 0 = visited, 1 = not visited, 2 = edge
    val map = heights.map { it.map { h -> if (h < 9) 1 else 2 }.toMutableList() }

    // Count number of locations that need to be visited
    val totalLocations = map.sumOf { it.count { v -> v == 1 } }
    var visitedLocations = 0

    val basinSizes = mutableListOf<Int>()
    while (visitedLocations < totalLocations) {
        // Find first seed point that has not been visited yet
        val queue = mutableListOf(seedPoints.first { map[it.i][it.j] == 1 })

        // Use region growing to measure size of the basin
        var currentBasinSize = 0
        while (queue.isNotEmpty()) {
            val currentLocation = queue.removeFirst()
            if (map[currentLocation.i][currentLocation.j] == 1) {
                currentBasinSize++

                // Mark location as visited
                visitedLocations++
                map[currentLocation.i][currentLocation.j] = 0

                // Add neighbors to queue
                queue.addAll(currentLocation.neighbors(map))
            }
        }

        basinSizes.add(currentBasinSize)
    }

    return basinSizes
}

fun main() {
    val heights  = File("inputs", "day9.txt").readLines().map {
        it.map { c -> c.digitToInt() }
    }

    val lowPoints = findLowPoints(heights)
    val riskLevel = lowPoints.sumOf { heights[it.i][it.j] + 1 }
    println("Solution 1: $riskLevel")

    val basins = measureBasins(heights, lowPoints)
    val basinScore = basins.sortedDescending().subList(0, 3).reduce { a, b -> a * b }
    println("Solution 2: $basinScore")
}
