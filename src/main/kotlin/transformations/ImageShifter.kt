package imageprocessing.transformations

import java.awt.image.BufferedImage
class ImageShifter {

    fun shiftImage(
        source: BufferedImage,
        shiftX: Int,
        shiftY: Int,
        borderColor: Triple<Int, Int, Int>
    ): BufferedImage {
        val width = source.width
        val height = source.height

        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val borderRGB = rgbToInt(borderColor.first, borderColor.second, borderColor.third)

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

    private fun rgbToInt(red: Int, green: Int, blue: Int): Int {
        return (red shl 16) or (green shl 8) or blue
    }
}
