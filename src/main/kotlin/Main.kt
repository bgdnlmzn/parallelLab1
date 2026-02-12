package imageprocessing

import imageprocessing.core.ImageProcessor
import imageprocessing.testing.PerformanceTester
import imageprocessing.utils.ImageGenerator
import java.io.File
import java.util.Locale

fun main(args: Array<String>) {
    val argsMap = parseArgs(args)
    if (argsMap.containsKey("help")) {
        printUsage()
        return
    }
    val settings = parseSettings(argsMap)
    val inputDir = settings.inputDir
    val outputDir = settings.outputDir
    val shiftX = settings.shiftX
    val shiftY = settings.shiftY
    val iterations = settings.iterations
    val borderColor = settings.borderColor
    val testSizes = settings.testSizes
    val threadCounts = settings.threadCounts

    println("=".repeat(80))
    println("Image processing program")
    println("=".repeat(80))
    inputDir.mkdirs()
    outputDir.mkdirs()
    generateTestImagesIfNeeded(inputDir, testSizes)

    val processor = ImageProcessor(shiftX, shiftY, borderColor)
    val tester = PerformanceTester(processor, outputDir)
    
    println("\nStarting performance tests...")
    println("Processing parameters:")
    println("  - Shift X: $shiftX pixels")
    println("  - Shift Y: $shiftY pixels")
    println("  - Border color: RGB(${borderColor.first}, ${borderColor.second}, ${borderColor.third})")
    println("  - Runs per size: $iterations")
    println("  - Thread counts: ${threadCounts.joinToString(", ")}")
    println()
    val results = tester.runThreadScalingTests(
        inputDir = inputDir,
        imageSizes = testSizes,
        threadCounts = threadCounts,
        iterations = iterations
    )

    printResults(results, threadCounts)
    
    println("\n" + "=".repeat(80))
    println("Processing completed!")
    println("Results saved to: ${outputDir.absolutePath}")
    println("=".repeat(80))
}

private data class Settings(
    val inputDir: File,
    val outputDir: File,
    val shiftX: Int,
    val shiftY: Int,
    val iterations: Int,
    val borderColor: Triple<Int, Int, Int>,
    val testSizes: List<Pair<Int, Int>>,
    val threadCounts: List<Int>
)

private fun parseSettings(args: Map<String, String>): Settings {
    val inputDir = File(args["inputDir"] ?: "test_images")
    val outputDir = File(args["outputDir"] ?: "output_images")
    val shiftX = args["shiftX"]?.toIntOrNull() ?: 50
    val shiftY = args["shiftY"]?.toIntOrNull() ?: 30
    val iterationsRaw = args["iterations"]?.toIntOrNull() ?: 3
    val iterations = if (iterationsRaw < 1) 1 else iterationsRaw
    val borderColor = parseBorderColor(args["borderColor"] ?: "187,38,73")
    val testSizes = parseSizes(args["sizes"] ?: "1024x768,1280x960,2048x1536")
    val threadCounts = parseThreadCounts(args["threads"] ?: "1,2,4,6,8,10,12,14,16")
    return Settings(
        inputDir = inputDir,
        outputDir = outputDir,
        shiftX = shiftX,
        shiftY = shiftY,
        iterations = iterations,
        borderColor = borderColor,
        testSizes = testSizes,
        threadCounts = threadCounts
    )
}

private fun generateTestImagesIfNeeded(inputDir: File, sizes: List<Pair<Int, Int>>) {
    var generated = false
    sizes.forEach { (width, height) ->
        val file = File(inputDir, "test_${width}x${height}.png")
        if (!file.exists()) {
            println("Generating test image: ${file.name}")
            ImageGenerator.generateColorfulImage(width, height, file)
            generated = true
        }
    }
    
    if (generated) {
        println("Test images created in: ${inputDir.absolutePath}\n")
    }
}

private fun parseArgs(args: Array<String>): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (arg == "--help" || arg == "-h") {
            result["help"] = "true"
            i++
            continue
        }
        if (arg.startsWith("--")) {
            val keyValue = arg.removePrefix("--")
            if (keyValue.contains("=")) {
                val parts = keyValue.split("=", limit = 2)
                val key = parts[0]
                val value = parts[1]
                result[key] = value
            } else {
                val value = if (i + 1 < args.size && !args[i + 1].startsWith("--")) {
                    i++
                    args[i]
                } else {
                    "true"
                }
                result[keyValue] = value
            }
        }
        i++
    }
    return result
}

private fun parseSizes(value: String): List<Pair<Int, Int>> {
    val parsed = value.split(",")
        .mapNotNull { item ->
            val parts = item.trim().lowercase().split("x")
            if (parts.size != 2) return@mapNotNull null
            val w = parts[0].toIntOrNull() ?: return@mapNotNull null
            val h = parts[1].toIntOrNull() ?: return@mapNotNull null
            if (w <= 0 || h <= 0) return@mapNotNull null
            Pair(w, h)
        }
    return parsed.ifEmpty {
        listOf(Pair(1024, 768), Pair(1280, 960), Pair(2048, 1536))
    }
}

private fun parseBorderColor(value: String): Triple<Int, Int, Int> {
    val parts = value.split(",").map { it.trim().toIntOrNull() ?: 0 }
    val r = parts.getOrElse(0) { 0 }.coerceIn(0, 255)
    val g = parts.getOrElse(1) { 0 }.coerceIn(0, 255)
    val b = parts.getOrElse(2) { 0 }.coerceIn(0, 255)
    return Triple(r, g, b)
}

private fun parseThreadCounts(value: String): List<Int> {
    val parsed = value.split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it > 0 }
        .distinct()
        .sorted()
    return parsed.ifEmpty { listOf(2, 4, 6, 8, 10, 12, 14, 16) }
}

private fun printUsage() {
    println(
        """
        Run parameters:
          --inputDir <path>
          --outputDir <path>
          --shiftX <number>
          --shiftY <number>
          --iterations <number>
          --sizes <list>
          --threads <list>
          --borderColor <rgb>
          -h, --help

        Examples:
          --sizes 1024x768,1280x960,2048x1536
          --threads 2,4,6,8,10,12,14,16
          --borderColor 187,38,73
        """.trimIndent()
    )
}

private fun printResults(
    results: List<PerformanceTester.TestResult>,
    threadCounts: List<Int>
) {
    println("\n" + "=".repeat(80))
    println("TEST RESULTS")
    println("=".repeat(80))

    val groupedBySize = results.groupBy { it.width to it.height }
    groupedBySize.forEach { (size, sizeResults) ->
        val width = size.first
        val height = size.second
        println("\nImage size: $width x $height")
        println("-".repeat(80))

        val byThreads = sizeResults.associateBy { it.threadCount }
        val baseline = byThreads[threadCounts.first()]?.averageTime

        threadCounts.forEach { threadCount ->
            val result = byThreads[threadCount] ?: return@forEach
            val speedup = if (baseline != null && result.averageTime > 0.0) {
                baseline / result.averageTime
            } else {
                1.0
            }
            println(
                "  Threads: ${result.threadCount.toString().padEnd(2)} " +
                    "Avg: ${formatTime(result.averageTime).padEnd(16)} " +
                    "Min: ${formatTime(result.minTime).padEnd(16)} " +
                    "Max: ${formatTime(result.maxTime).padEnd(16)} " +
                    "StdDev: ${formatTime(result.standardDeviation).padEnd(16)} " +
                    "Speedup: ${String.format(Locale.US, "%.2fx", speedup)}"
            )
        }
    }
}

private fun formatTime(milliseconds: Number): String {
    val ms = milliseconds.toDouble()
    return when {
        ms < 1000 -> String.format(Locale.US, "%.2f ms", ms)
        else -> String.format(Locale.US, "%.2f sec (%.0f ms)", ms / 1000, ms)
    }
}
