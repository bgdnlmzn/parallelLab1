package imageprocessing.io

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageLoader {

    fun loadImage(file: File): BufferedImage {
        require(file.exists()) { "File not found: ${file.absolutePath}" }
        require(file.isFile) { "Path is not a file: ${file.absolutePath}" }

        return try {
            val image = ImageIO.read(file)
                ?: throw IllegalArgumentException("Failed to read image from file: ${file.name}")

            convertToRGB(image)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error loading image: ${e.message}", e)
        }
    }

    private fun convertToRGB(source: BufferedImage): BufferedImage {
        if (source.type == BufferedImage.TYPE_INT_RGB) {
            return source
        }
        val converted = BufferedImage(
            source.width,
            source.height,
            BufferedImage.TYPE_INT_RGB
        )
        val g = converted.createGraphics()
        g.drawImage(source, 0, 0, null)
        g.dispose()
        return converted
    }
}

class ImageSaver {

    fun saveImage(
        image: BufferedImage,
        file: File,
        format: ImageFormat = ImageFormat.PNG
    ) {
        try {
            file.parentFile?.mkdirs()
            val success = ImageIO.write(image, format.extension, file)
            if (!success) {
                throw IllegalArgumentException(
                    "Failed to save image. Format ${format.extension} may not be supported"
                )
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error saving image: ${e.message}", e)
        }
    }
}

enum class ImageFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
    BMP("bmp", "image/bmp"),
    GIF("gif", "image/gif")
}
