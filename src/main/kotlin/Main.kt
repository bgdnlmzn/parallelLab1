package imageprocessing

import imageprocessing.core.ImageProcessor
import imageprocessing.testing.PerformanceTester
import imageprocessing.utils.ImageGenerator
import java.io.File

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
    println()
    val results = tester.runTests(inputDir, testSizes, iterations = iterations)

    printResults(results)
    
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
    val testSizes: List<Pair<Int, Int>>
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
    return Settings(
        inputDir = inputDir,
        outputDir = outputDir,
        shiftX = shiftX,
        shiftY = shiftY,
        iterations = iterations,
        borderColor = borderColor,
        testSizes = testSizes
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
          --borderColor <rgb>
          -h, --help

        Examples:
          --sizes 1024x768,1280x960,2048x1536
          --borderColor 187,38,73
        """.trimIndent()
    )
}

private fun printResults(results: List<PerformanceTester.TestResult>) {
    println("\n" + "=".repeat(80))
    println("TEST RESULTS")
    println("=".repeat(80))
    
    results.forEach { result ->
        println("\nImage size: ${result.width} x ${result.height}")
        println("-".repeat(60))
        println("  All timing measurements:")
        result.timings.forEachIndexed { index, time ->
            println("    Run ${index + 1}: ${formatTime(time)}")
        }
        println("  Average time: ${formatTime(result.averageTime)}")
        println("  Min time: ${formatTime(result.minTime)}")
        println("  Max time: ${formatTime(result.maxTime)}")
        println("  Standard deviation: ${formatTime(result.standardDeviation)}")
        println("  Output file: ${result.outputFile}")
    }
    
    println("\n" + "=".repeat(80))
    println("PERFORMANCE COMPARISON")
    println("=".repeat(80))
    
    if (results.size > 1) {
        val baseline = results.first()
        results.drop(1).forEach { result ->
            val ratio = result.averageTime / baseline.averageTime
            val pixels = result.width * result.height
            val baselinePixels = baseline.width * baseline.height
            val pixelRatio = pixels.toDouble() / baselinePixels.toDouble()
            
            println("\n${result.width}x${result.height} vs ${baseline.width}x${baseline.height}:")
            println("  Pixel count increase: ${String.format("%.2fx", pixelRatio)}")
            println("  Processing time increase: ${String.format("%.2fx", ratio)}")
            println("  Efficiency: ${String.format("%.2f%%", (pixelRatio / ratio) * 100)}")
        }
    }
}

private fun formatTime(milliseconds: Number): String {
    val ms = milliseconds.toDouble()
    return when {
        ms < 1000 -> String.format("%.2f ms", ms)
        else -> String.format("%.2f sec (%.0f ms)", ms / 1000, ms)
    }
}
