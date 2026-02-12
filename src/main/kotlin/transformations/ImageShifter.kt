package imageprocessing.transformations

import java.awt.image.BufferedImage
import java.util.concurrent.Executors
class ImageShifter {

    fun shiftImage(
        source: BufferedImage,
        shiftX: Int,
        shiftY: Int,
        borderColor: Triple<Int, Int, Int>,
        threadCount: Int
    ): BufferedImage {
        val width = source.width
        val height = source.height

        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val borderRGB = rgbToInt(borderColor.first, borderColor.second, borderColor.third)

        if (threadCount <= 1) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val sourceX = x - shiftX
                    val sourceY = y - shiftY

                    val rgb = if (sourceX in 0 until width && sourceY in 0 until height) {
                        source.getRGB(sourceX, sourceY)
                    } else {
                        borderRGB
                    }
                    result.setRGB(x, y, rgb)
                }
            }
            return result
        }

        parallelForRows(height, threadCount) { startRow, endRow ->
            for (y in startRow until endRow) {
                for (x in 0 until width) {
                    val sourceX = x - shiftX
                    val sourceY = y - shiftY

                    val rgb = if (sourceX in 0 until width && sourceY in 0 until height) {
                        source.getRGB(sourceX, sourceY)
                    } else {
                        borderRGB
                    }
                    result.setRGB(x, y, rgb)
                }
            }
        }
        return result
    }

    private fun rgbToInt(red: Int, green: Int, blue: Int): Int {
        return (red shl 16) or (green shl 8) or blue
    }

    private fun parallelForRows(
        totalRows: Int,
        threadCount: Int,
        action: (startRow: Int, endRow: Int) -> Unit
    ) {
        val pool = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<java.util.concurrent.Future<*>>()
        val chunkSize = (totalRows + threadCount - 1) / threadCount
        for (i in 0 until threadCount) {
            val start = i * chunkSize
            if (start >= totalRows) break
            val end = minOf(totalRows, start + chunkSize)
            futures += pool.submit { action(start, end) }
        }
        futures.forEach { it.get() }
        pool.shutdown()
    }
}
