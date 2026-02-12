package imageprocessing.testing

import imageprocessing.core.ImageProcessor
import java.io.File
import kotlin.math.sqrt
class PerformanceTester(
    private val processor: ImageProcessor,
    private val outputDir: File
) {

    fun runTests(
        inputDir: File,
        imageSizes: List<Pair<Int, Int>>,
        iterations: Int = 3
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        imageSizes.forEach { (width, height) ->
            println("\n${"─".repeat(60)}")
            println("Testing size: $width x $height")
            println("─".repeat(60))
            
            val inputFile = File(inputDir, "test_${width}x${height}.png")
            
            if (!inputFile.exists()) {
                println("⚠ File not found: ${inputFile.name}")
                return@forEach
            }
            
            val timings = mutableListOf<Long>()
            
            repeat(iterations) { iteration ->
                val outputFile = File(
                    outputDir,
                    "output_${width}x${height}_run${iteration + 1}.png"
                )

                if (iteration == 0) {
                    print("  JVM warm-up... ")
                    processor.processImage(inputFile, outputFile)
                    println("done")
                }

                System.gc()
                Thread.sleep(100)
                print("  Run ${iteration + 1}/$iterations... ")
                val time = processor.processImage(inputFile, outputFile)
                timings.add(time)
                println("${time} ms")
            }

            val finalOutputFile = File(outputDir, "result_${width}x${height}.png")
            val finalInputFile = File(inputDir, "test_${width}x${height}.png")
            processor.processImage(finalInputFile, finalOutputFile)

            val result = TestResult(
                width = width,
                height = height,
                timings = timings,
                outputFile = finalOutputFile.name
            )
            
            results.add(result)
            
            println("  ✓ Average time: ${String.format("%.2f", result.averageTime)} ms")
        }
        
        return results
    }

    data class TestResult(
        val width: Int,
        val height: Int,
        val timings: List<Long>,
        val outputFile: String
    ) {
        val averageTime: Double = timings.average()

        val minTime: Long = timings.minOrNull() ?: 0L

        val maxTime: Long = timings.maxOrNull() ?: 0L

        val standardDeviation: Double by lazy {
            if (timings.isEmpty()) return@lazy 0.0
            val mean = averageTime
            val variance = timings.map { (it - mean) * (it - mean) }.average()
            sqrt(variance)
        }
    }
}
