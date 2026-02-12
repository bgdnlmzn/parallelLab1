package imageprocessing.filters

import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

class ConvolutionFilter {

    fun applyFilter(source: BufferedImage, kernel: ConvolutionKernel): BufferedImage {
        val width = source.width
        val height = source.height
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val kernelSize = kernel.size
        val offset = kernelSize / 2

        for (y in 0 until height) {
            for (x in 0 until width) {
                val red = applyKernelToChannel(source, x, y, offset, kernel) { rgb ->
                    (rgb shr 16) and 0xFF
                }
                val green = applyKernelToChannel(source, x, y, offset, kernel) { rgb ->
                    (rgb shr 8) and 0xFF
                }
                val blue = applyKernelToChannel(source, x, y, offset, kernel) { rgb ->
                    rgb and 0xFF
                }

                val rgb = (clamp(red) shl 16) or (clamp(green) shl 8) or clamp(blue)
                result.setRGB(x, y, rgb)
            }
        }
        return result
    }

    private fun applyKernelToChannel(
        source: BufferedImage,
        x: Int,
        y: Int,
        offset: Int,
        kernel: ConvolutionKernel,
        channelExtractor: (Int) -> Int
    ): Int {
        var sum = 0.0

        for (ky in 0 until kernel.size) {
            for (kx in 0 until kernel.size) {
                val pixelX = x + kx - offset
                val pixelY = y + ky - offset

                if (pixelX >= 0 && pixelX < source.width && 
                    pixelY >= 0 && pixelY < source.height) {
                    val rgb = source.getRGB(pixelX, pixelY)
                    val channelValue = channelExtractor(rgb)
                    sum += channelValue * kernel.getValue(kx, ky)
                }
            }
        }

        return ((sum / kernel.divisor) + kernel.offset).toInt()
    }

    private fun clamp(value: Int): Int = max(0, min(255, value))
}

data class ConvolutionKernel(
    val size: Int,
    val matrix: Array<DoubleArray>,
    val divisor: Double = 1.0,
    val offset: Double = 0.0
) {
    init {
        require(size % 2 == 1) { "Kernel size must be an odd number" }
        require(matrix.size == size) { "Matrix size does not match kernel size" }
        require(matrix.all { it.size == size }) { "Matrix must be square" }
    }

    fun getValue(x: Int, y: Int): Double = matrix[y][x]
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConvolutionKernel) return false
        if (size != other.size) return false
        if (divisor != other.divisor) return false
        if (offset != other.offset) return false
        if (!matrix.contentDeepEquals(other.matrix)) return false
        return true
    }
    override fun hashCode(): Int {
        var result = size
        result = 31 * result + matrix.contentDeepHashCode()
        result = 31 * result + divisor.hashCode()
        result = 31 * result + offset.hashCode()
        return result
    }
}
