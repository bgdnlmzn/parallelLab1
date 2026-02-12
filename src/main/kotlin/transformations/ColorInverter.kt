package imageprocessing.transformations

import imageprocessing.parallel.RowParallelExecutor
import java.awt.image.BufferedImage

class ColorInverter {
    fun invert(source: BufferedImage, threadCount: Int): BufferedImage {
        val width = source.width
        val height = source.height
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        RowParallelExecutor.run(height, threadCount) { startRow, endRow ->
            for (y in startRow until endRow) {
                for (x in 0 until width) {
                    val rgb = source.getRGB(x, y)
                    val red = 255 - ((rgb shr 16) and 0xFF)
                    val green = 255 - ((rgb shr 8) and 0xFF)
                    val blue = 255 - (rgb and 0xFF)
                    val inverted = (red shl 16) or (green shl 8) or blue
                    result.setRGB(x, y, inverted)
                }
            }
        }

        return result
    }
}
