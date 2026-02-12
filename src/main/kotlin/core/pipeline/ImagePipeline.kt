package imageprocessing.core.pipeline

import java.awt.image.BufferedImage

interface ImagePipeline {
    val name: String
    fun apply(image: BufferedImage, threadCount: Int): BufferedImage
}
