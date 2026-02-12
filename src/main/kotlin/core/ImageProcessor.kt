package imageprocessing.core

import imageprocessing.filters.ConvolutionFilter
import imageprocessing.filters.GaussianBlurKernel
import imageprocessing.io.ImageLoader
import imageprocessing.io.ImageSaver
import imageprocessing.transformations.ImageShifter
import java.awt.image.BufferedImage
import java.io.File

class ImageProcessor(
    private val shiftX: Int,
    private val shiftY: Int,
    private val borderColor: Triple<Int, Int, Int>
) {
    private val imageLoader = ImageLoader()
    private val imageSaver = ImageSaver()
    private val imageShifter = ImageShifter()
    private val convolutionFilter = ConvolutionFilter()
    
    fun processImage(inputFile: File, outputFile: File): Long {
        val startTime = System.currentTimeMillis()

        val originalImage = imageLoader.loadImage(inputFile)

        val processedImage = applyTransformations(originalImage)

        imageSaver.saveImage(processedImage, outputFile)
        val endTime = System.currentTimeMillis()
        return endTime - startTime
    }

    private fun applyTransformations(image: BufferedImage): BufferedImage {
        val shiftedImage = imageShifter.shiftImage(
            image, 
            shiftX, 
            shiftY, 
            borderColor
        )

        val kernel = GaussianBlurKernel.create3x3()
        val blurredImage = convolutionFilter.applyFilter(shiftedImage, kernel)
        return blurredImage
    }

}
