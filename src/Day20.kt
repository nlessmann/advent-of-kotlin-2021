import java.io.File

class Image(val pixels: List<List<Int>>, val infinityValue: Int = 0) {
    fun countForegroundPixels(): Int {
        return pixels.sumOf { row ->
            row.count { it == 1 }
        }
    }
}

class ImageEnhancer(private val replacements: List<Int>) {
    fun enhance(image: Image): Image {
        val input = image.pixels

        val rows = input.indices
        val cols = input.first().indices

        val width = rows.last + 1
        val height = cols.last + 1

        // Transform the pixels using a 3x3 sliding window
        val transformedPixels = buildList {
            for (r in -1 until width + 1) {
                add(buildList {
                    for (c in -1 until height + 1) {
                        val neighborIndices = listOf(
                            r - 1 to c - 1, r - 1 to c, r - 1 to c + 1,
                            r to c - 1, r to c, r to c + 1,
                            r + 1 to c - 1, r + 1 to c, r + 1 to c + 1
                        )
                        val neighborValues = neighborIndices.map { idx ->
                            if (idx.first in rows && idx.second in cols) {
                                input[idx.first][idx.second]
                            } else {
                                image.infinityValue
                            }
                        }
                        add(lookupReplacement(neighborValues))
                    }
                })
            }
        }

        // Transform infinity value as well
        val transformedInfinityValue = lookupReplacement(
            generateSequence { image.infinityValue }.take(9).toList()
        )

        return Image(transformedPixels, transformedInfinityValue)
    }

    fun enhance(image: Image, n: Int): Image {
        var enhancedImage = image
        for (i in 1..n) {
            enhancedImage = enhance(enhancedImage)
        }
        return enhancedImage
    }

    private fun lookupReplacement(bits: Word): Int {
        return replacements[binaryToDecimal(bits)]
    }
}

fun main() {
    val input = File("inputs", "day20.txt").readLines()

    // First line of the input is the definition of the image enhancement algorithm
    val enhancer = ImageEnhancer(input.first().trim().map { if (it == '#') 1 else 0 })

    // Remaining lines define the initial unenhanced image
    val image = Image(
        input.subList(2, input.size).map { row ->
            row.trim().map { if (it == '#') 1 else 0 }
        }
    )

    // Enhance image twice
    val enhancedImage1 = enhancer.enhance(image, 2)
    println("Solution 1: ${enhancedImage1.countForegroundPixels()}")

    // Enhance image 50 times in total, so 48 more times
    val enhancedImage2 = enhancer.enhance(enhancedImage1, 48)
    println("Solution 2: ${enhancedImage2.countForegroundPixels()}")
}
