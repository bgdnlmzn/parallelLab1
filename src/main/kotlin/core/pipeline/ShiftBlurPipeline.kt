package imageprocessing.core.pipeline

import imageprocessing.filters.ConvolutionFilter
import imageprocessing.filters.GaussianBlurKernel
import imageprocessing.transformations.ImageShifter
import java.awt.image.BufferedImage

class ShiftBlurPipeline(
    private val shiftX: Int,
    private val shiftY: Int,
    private val borderColor: Triple<Int, Int, Int>
) : ImagePipeline {
    override val name: String = "shift-blur"

    private val imageShifter = ImageShifter()
    private val convolutionFilter = ConvolutionFilter()

    override fun apply(image: BufferedImage, threadCount: Int): BufferedImage {
        val shifted = imageShifter.shiftImage(
            source = image,
            shiftX = shiftX,
            shiftY = shiftY,
            borderColor = borderColor,
            threadCount = threadCount
        )
        val kernel = GaussianBlurKernel.create3x3()
        return convolutionFilter.applyFilter(shifted, kernel, threadCount)
    }
}
