package imageprocessing.utils

import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
object ImageGenerator {

    fun generateColorfulImage(width: Int, height: Int, outputFile: File) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d: Graphics2D = image.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val gradient = GradientPaint(
            0f, 0f, Color(30, 60, 114),
            width.toFloat(), height.toFloat(), Color(42, 82, 152)
        )
        g2d.paint = gradient
        g2d.fillRect(0, 0, width, height)

        val random = Random(42)
        repeat(20) {
            val x = random.nextInt(width)
            val y = random.nextInt(height)
            val radius = random.nextInt(50, 150)
            val alpha = random.nextInt(100, 200)
            val color = Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256),
                alpha
            )
            g2d.color = color
            g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2)
        }

        drawWavePattern(g2d, width, height)

        g2d.color = Color.WHITE
        g2d.font = g2d.font.deriveFont(24f)
        val text = "Test Image ${width}×${height}"
        val fm = g2d.fontMetrics
        val textWidth = fm.stringWidth(text)
        g2d.drawString(text, (width - textWidth) / 2, height / 2)
        g2d.dispose()

        outputFile.parentFile?.mkdirs()
        ImageIO.write(image, "png", outputFile)
    }

    private fun drawWavePattern(g2d: Graphics2D, width: Int, height: Int) {
        g2d.color = Color(255, 255, 255, 30)
        for (i in 0 until width step 10) {
            val x = i
            val y1 = (height / 2 + sin(i * 0.02) * 100).toInt()
            val y2 = (height / 2 + cos(i * 0.02) * 100).toInt()
            g2d.drawLine(x, y1, x, y2)
        }
    }
}
