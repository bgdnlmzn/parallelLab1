package imageprocessing.core.pipeline

import imageprocessing.filters.ConvolutionFilter
import imageprocessing.filters.ContrastKernel
import imageprocessing.transformations.ColorInverter
import java.awt.image.BufferedImage

class InvertContrastPipeline : ImagePipeline {
    override val name: String = "invert-contrast"

    private val inverter = ColorInverter()
    private val convolutionFilter = ConvolutionFilter()

    override fun apply(image: BufferedImage, threadCount: Int): BufferedImage {
        val inverted = inverter.invert(image, threadCount)
        val kernel = ContrastKernel.create3x3()
        return convolutionFilter.applyFilter(inverted, kernel, threadCount)
    }
}
