package imageprocessing.core

import imageprocessing.core.pipeline.ImagePipeline
import imageprocessing.io.ImageLoader
import imageprocessing.io.ImageSaver
import java.io.File

class ImageProcessor(
    private val pipeline: ImagePipeline
) {
    private val imageLoader = ImageLoader()
    private val imageSaver = ImageSaver()
    
    fun processImage(inputFile: File, outputFile: File, threadCount: Int): Long {
        val startTime = System.currentTimeMillis()

        val originalImage = imageLoader.loadImage(inputFile)

        val processedImage = pipeline.apply(originalImage, threadCount)

        imageSaver.saveImage(processedImage, outputFile)
        val endTime = System.currentTimeMillis()
        return endTime - startTime
    }
}
