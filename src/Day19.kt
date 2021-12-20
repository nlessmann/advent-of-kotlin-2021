import java.io.File
import kotlin.math.absoluteValue

data class Vector(val x: Int, val y: Int, val z: Int) {
    private fun differences(other: Vector): Iterable<Int> {
        return listOf(x - other.x, y - other.y, z - other.z)
    }

    fun squaredDistanceTo(other: Vector): Long {
        return differences(other).sumOf { (it * it).toLong() }
    }

    fun manhattanDistanceTo(other: Vector): Long {
        return differences(other).sumOf { it.absoluteValue.toLong() }
    }

    operator fun plus(other: Vector): Vector {
        return Vector(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vector): Vector {
        return Vector(x - other.x, y - other.y, z - other.z)
    }
}

class FlipMatrix(x: List<Int>, y: List<Int>) {
    private val values: List<Int> = buildList {
        // The matrix is stored in column-major order
        addAll(x)
        addAll(y)

        // Compute cross product of vectors to determine z
        add(x[1] * y[2] - x[2] * y[1])
        add(x[2] * y[0] - x[0] * y[2])
        add(x[0] * y[1] - x[1] * y[0])
    }

    fun isValid(): Boolean {
        // A valid right-handed coordinate system basis has determinant 1
        val det = (
            values[0] * (values[4] * values[8] - values[7] * values[5]) -
            values[3] * (values[1] * values[8] - values[7] * values[2]) +
            values[6] * (values[1] * values[5] - values[4] * values[2])
        )

        return det == 1
    }

    operator fun times(beacon: Vector): Vector {
        return Vector(
            beacon.x * values[0] + beacon.y * values[3] + beacon.z * values[6],
            beacon.x * values[1] + beacon.y * values[4] + beacon.z * values[7],
            beacon.x * values[2] + beacon.y * values[5] + beacon.z * values[8]
        )
    }
}

class Transformation(val offset: Vector, val rotation: FlipMatrix) {
    fun transform(vector: Vector): Vector {
        return rotation * vector + offset
    }
}

class BeaconTrio(a: Vector, b: Vector, c: Vector) {
    val beacons = setOf(a, b, c)

    private val fingerprint: List<Long>

    init {
        // Compute sum of distance to the other two points for each point
        val ab = a.squaredDistanceTo(b)
        val bc = b.squaredDistanceTo(c)
        val ac = a.squaredDistanceTo(c)

        fingerprint = listOf(ab + ac, ab + bc, ac + bc).sorted()
    }

    override fun equals(other: Any?): Boolean {
        // Trios with the same fingerprint are considered equal
        return if (other is BeaconTrio) {
            fingerprint == other.fingerprint
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return fingerprint.hashCode()
    }
}

class Scanner(input: List<String>) {
    val id: Int
    val beacons: List<Vector>

    private val trios: List<BeaconTrio>
    private val transforms = mutableListOf<Transformation>()

    init {
        // Extract scanner ID from the first line
        val match = Regex("--- scanner ([0-9]+) ---").matchEntire(input.first().trim())
            ?: throw IllegalArgumentException("Malformed scanner input")
        id = match.groupValues[1].toInt()

        // Extract beacon signals from the other lines
        beacons = input.drop(1).map { line ->
            line.trim().split(',').let { coords ->
                Vector(coords[0].toInt(), coords[1].toInt(), coords[2].toInt())
            }
        }

        // Iterate over all combinations of three beacons and group them into a trio
        trios = buildList {
            for (i in 0 until beacons.size - 2) {
                for (j in i + 1 until beacons.size - 1) {
                    for (k in j + 1 until beacons.size) {
                        add(BeaconTrio(beacons[i], beacons[j], beacons[k]))
                    }
                }
            }
        }
    }

    private fun findOffset(a: List<Vector>, b: List<Vector>, rotation: FlipMatrix): Vector? {
        // Use provided lists of beacons and rotation matrix to find the offset
        val rotatedBeacons = b.map { rotation * it }

        for (referenceBeacon in a) {
            for (rotatedBeacon in rotatedBeacons) {
                val offset = referenceBeacon - rotatedBeacon
                val alignedBeacons = rotatedBeacons.map { it + offset }
                val intersection = a.intersect(alignedBeacons.toSet())
                if (intersection.size >= 12) {
                    return offset
                }
            }
        }

        return null
    }

    fun alignWith(other: Scanner): Boolean {
        // Attempt to find the position of this scanner relative to the other scanner
        val matches = other.trios.mapNotNull { a ->
            trios.singleOrNull { b -> a == b }?.let { b -> a to b }
        }

        val matchingBeaconsA = matches.flatMap { it.first.beacons }.distinct()
        val matchingBeaconsB = matches.flatMap { it.second.beacons }.distinct()

        for (orientation in orientations) {
            val offset = findOffset(matchingBeaconsA, matchingBeaconsB, orientation)
            if (offset != null) {
                transforms.addAll(other.transforms)
                transforms.add(Transformation(offset, orientation))
                return true
            }
        }

        return false
    }

    fun transform(): Vector {
        // Return scanner location relative to reference scanner
        val origin = Vector(0, 0, 0)
        return transforms.reversed().fold(origin) { v, p -> p.transform(v) }
    }

    fun transformBeacons(): List<Vector> {
        // Return beacon locations relative to reference scanner
        return beacons.map {
            transforms.reversed().fold(it) { v, p -> p.transform(v) }
        }
    }

    companion object {
        val orientations: List<FlipMatrix>

        init {
            val candidateVectors = listOf(
                listOf(0, 0, 1), listOf(0, 0, -1),
                listOf(0, 1, 0), listOf(0, -1, 0),
                listOf(1, 0, 0), listOf(-1, 0, 0)
            )

            val matrices = mutableListOf<FlipMatrix>()
            for (x in candidateVectors) {
                for (y in candidateVectors) {
                    if (x != y) {
                        matrices.add(FlipMatrix(x, y))
                    }
                }
            }

            orientations = matrices.filter { it.isValid() }
        }
    }
}

fun parseScannerData(filename: String): List<Scanner> {
    val input = File(filename).readLines()

    // Find indices of lines that specify the scanner ID
    val indices = input.mapIndexedNotNull { index, line ->
        if (line.startsWith("---")) index else null
    }.toMutableList()

    // Add index at the end, chop into chunks and convert into scanner instances
    indices.add(input.size + 1)
    return indices.windowed(2)
        .map { (i, j) -> input.subList(i, j - 1) }
        .map { Scanner(it) }
}

fun main() {
    val scanners = parseScannerData("inputs/day19.txt")

    // Match scanning results with each other
    val referenceScanners = mutableListOf(scanners[0])
    val located = referenceScanners.toMutableSet()

    while (located.size < scanners.size && referenceScanners.isNotEmpty()) {
        val referenceScanner = referenceScanners.removeFirst()
        for (scanner in scanners.filter { it !in located }) {
            if (scanner.alignWith(referenceScanner)) {
                println("Found scanner-to-scanner transformation: ${scanner.id} -> ${referenceScanner.id}")
                located.add(scanner)
                referenceScanners.add(scanner)
            }
        }

        println("Still need to find ${scanners.size - located.size} transformations" )
    }

    println("----------")

    // Transform all beacons and determine number of unique beacons
    val beacons = scanners.flatMap { it.transformBeacons() }.distinct()
    println("Solution 1: ${beacons.size}")

    // Transform all scanner locations and find maximum scanner distance
    val scannerLocations = scanners.map { it.transform() }
    val scannerDistances = buildList {
        for (i in 0 until scanners.size - 1) {
            for (j in i + 1 until scanners.size) {
                add(scannerLocations[i].manhattanDistanceTo(scannerLocations[j]))
            }
        }
    }
    println("Solution 2: ${scannerDistances.maxOf { it }}")
}
