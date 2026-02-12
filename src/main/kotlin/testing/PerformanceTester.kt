package imageprocessing.testing

import imageprocessing.core.ImageProcessor
import java.io.File
import java.util.Locale
import kotlin.math.sqrt

class PerformanceTester(
    private val processor: ImageProcessor,
    private val outputDir: File
) {

    fun runThreadScalingTests(
        inputDir: File,
        imageSizes: List<Pair<Int, Int>>,
        threadCounts: List<Int>,
        iterations: Int = 3
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()

        threadCounts.forEach { threadCount ->
            println("\n" + "=".repeat(80))
            println("Running tests with thread count: $threadCount")
            println("=".repeat(80))

            imageSizes.forEach { (width, height) ->
                println("\n" + "-".repeat(80))
                println("Testing size: $width x $height")
                println("-".repeat(80))

                val inputFile = File(inputDir, "test_${width}x${height}.png")

                if (!inputFile.exists()) {
                    println("WARNING: File not found: ${inputFile.name}")
                    return@forEach
                }

                val timings = mutableListOf<Long>()
                repeat(iterations) { iteration ->
                    val outputFile = File(
                        outputDir,
                        "output_${width}x${height}_t${threadCount}_run${iteration + 1}.png"
                    )

                    if (iteration == 0) {
                        print("  Warm-up (threads=$threadCount)... ")
                        processor.processImage(inputFile, outputFile, threadCount)
                        println("done")
                    }

                    System.gc()
                    Thread.sleep(100)
                    print("  Run ${iteration + 1}/$iterations (threads=$threadCount)... ")
                    val time = processor.processImage(inputFile, outputFile, threadCount)
                    timings.add(time)
                    println("${time} ms")
                }

                val finalOutputFile = File(outputDir, "result_${width}x${height}_t${threadCount}.png")
                processor.processImage(inputFile, finalOutputFile, threadCount)

                val result = TestResult(
                    width = width,
                    height = height,
                    threadCount = threadCount,
                    timings = timings,
                    outputFile = finalOutputFile.name
                )
                results.add(result)
                println("  [OK] Average time: ${String.format(Locale.US, "%.2f", result.averageTime)} ms")
            }
        }

        return results
    }

    data class TestResult(
        val width: Int,
        val height: Int,
        val threadCount: Int,
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
