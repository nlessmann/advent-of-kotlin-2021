import java.io.File

class SeaFloor(filename: String) {
    private enum class SeaCucumber {
        EAST, SOUTH
    }

    private class SeaFloorTile(var occupant: SeaCucumber?) {
        val neighbors = mutableMapOf<SeaCucumber, SeaFloorTile>()

        fun canMoveOccupant(): Boolean {
            // No occupant, nothing to do
            if (occupant == null) return false

            // Check whether target tile is already occupied
            return neighbors.getValue(occupant!!).occupant == null
        }

        fun moveOccupant() {
            neighbors.getValue(occupant!!).occupant = occupant
            occupant = null
        }
    }

    private val tiles: List<SeaFloorTile>

    init {
        // Read initial state from file and parse into grid
        val input = File(filename).readLines()
        val grid = input.map { line ->
            line.map { c ->
                SeaFloorTile(when(c) {
                    'v' -> SeaCucumber.SOUTH
                    '>' -> SeaCucumber.EAST
                    else -> null
                })
            }
        }

        // Add neighbors to each tile
        grid.forEachIndexed { r, tiles ->
            tiles.forEachIndexed { c, tile ->
                tile.neighbors[SeaCucumber.SOUTH] = grid[if (r + 1 < grid.size) r + 1 else 0][c]
                tile.neighbors[SeaCucumber.EAST] = grid[r][if (c + 1 < tiles.size) c + 1 else 0]
            }
        }

        tiles = grid.flatten()
    }

    fun findEquilibrium(): Int {
        var steps = 0
        do {
            val moves = moveSeaCucumbers(SeaCucumber.EAST) + moveSeaCucumbers(SeaCucumber.SOUTH)
            steps++
        } while (moves > 0)
        return steps
    }

    private fun moveSeaCucumbers(herd: SeaCucumber): Int {
        val actionableTiles = tiles.filter { it.occupant == herd && it.canMoveOccupant() }
        actionableTiles.forEach { it.moveOccupant() }
        return actionableTiles.size
    }
}

fun main() {
    val floor = SeaFloor("inputs/day25.txt")
    println("Solution 1: ${floor.findEquilibrium()}")
}
