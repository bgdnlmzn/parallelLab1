package imageprocessing.filters

object ContrastKernel {
    fun create3x3(): ConvolutionKernel {
        val matrix = arrayOf(
            doubleArrayOf(0.0, -1.0, 0.0),
            doubleArrayOf(-1.0, 5.0, -1.0),
            doubleArrayOf(0.0, -1.0, 0.0)
        )
        return ConvolutionKernel(
            size = 3,
            matrix = matrix,
            divisor = 1.0,
            offset = 0.0
        )
    }
}
