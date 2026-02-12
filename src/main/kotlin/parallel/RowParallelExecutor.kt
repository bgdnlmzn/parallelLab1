package imageprocessing.parallel

import java.util.concurrent.Executors
import java.util.concurrent.Future

object RowParallelExecutor {
    fun run(
        totalRows: Int,
        threadCount: Int,
        action: (startRow: Int, endRow: Int) -> Unit
    ) {
        if (threadCount <= 1) {
            action(0, totalRows)
            return
        }

        val pool = Executors.newFixedThreadPool(threadCount)
        val futures = mutableListOf<Future<*>>()
        val chunkSize = (totalRows + threadCount - 1) / threadCount

        for (i in 0 until threadCount) {
            val start = i * chunkSize
            if (start >= totalRows) break
            val end = minOf(totalRows, start + chunkSize)
            futures += pool.submit { action(start, end) }
        }

        futures.forEach { it.get() }
        pool.shutdown()
    }
}
