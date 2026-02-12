package imageprocessing.filters

object GaussianBlurKernel {

    fun create3x3(): ConvolutionKernel {
        val matrix = arrayOf(
            doubleArrayOf(1.0, 2.0, 1.0),
            doubleArrayOf(2.0, 4.0, 2.0),
            doubleArrayOf(1.0, 2.0, 1.0)
        )

        val divisor = 16.0

        return ConvolutionKernel(
            size = 3,
            matrix = matrix,
            divisor = divisor,
            offset = 0.0
        )
    }
}
