import java.io.File

class Packet(bits: Word) {
    private val version = binaryToDecimal(bits.subList(0, 3))
    private val type = binaryToDecimal(bits.subList(3, 6))
    private val operands: List<Packet>

    val value: Long

    private val unconsumedPayload: Word

    init {
        var payload = bits.subList(6, bits.size).toList()

        if (type == 4) {
            // Parse content in blocks of 5 bits
            val content = mutableListOf<Int>()
            while (payload.size >= 5) {
                val lastByte = (payload.first() == 0)
                content.addAll(payload.subList(1, 5))
                payload = payload.subList(5, payload.size)

                if (lastByte) break
            }

            value = content.joinToString("").toLong(2)
            unconsumedPayload = payload.toList()
            operands = listOf()
        } else {
            // Parse operator depending on content length type
            val lengthType = payload.first()
            val packets = mutableListOf<Packet>()

            if (lengthType == 0) {
                // Total length of payload is given
                val length = binaryToDecimal(payload.subList(1, 16))
                unconsumedPayload = payload.subList(16 + length, payload.size).toList()
                payload = payload.subList(16, 16 + length)

                while (payload.any { it != 0 }) {
                    val packet = Packet(payload)
                    packets.add(packet)
                    payload = packet.unconsumedPayload
                }
            } else {
                // Number of sub-packets is given
                val n = binaryToDecimal(payload.subList(1, 12))
                payload = payload.subList(12, payload.size)
                for (i in 1..n) {
                    val packet = Packet(payload)
                    packets.add(packet)
                    payload = packet.unconsumedPayload
                }

                unconsumedPayload = payload.toList()
            }

            operands = packets

            // Compute value
            value = when (type) {
                0 -> operands.sumOf { it.value }
                1 -> operands.fold(1) { a, b -> a * b.value }
                2 -> operands.minOf { it.value }
                3 -> operands.maxOf { it.value }
                5 -> if (operands.first().value > operands.last().value) 1 else 0
                6 -> if (operands.first().value < operands.last().value) 1 else 0
                7 -> if (operands.first().value == operands.last().value) 1 else 0
                else -> throw IllegalArgumentException("Unknown operation type")
            }
        }
    }

    fun sumOfVersions(): Int {
        // Recursively add up the version numbers
        return version + operands.sumOf { it.sumOfVersions() }
    }

    companion object {
        fun fromHexString(message: String): Packet {
            return Packet(
                message.map {
                    it.digitToInt(16).toString(2).padStart(4, '0')
                }.joinToString("").map {
                    it.digitToInt()
                }
            )
        }
    }
}

fun main() {
    val input = File("inputs", "day16.txt").readText().trim()
    val packet = Packet.fromHexString(input)

    println("Solution 1: ${packet.sumOfVersions()}")
    println("Solution 2: ${packet.value}")
}
