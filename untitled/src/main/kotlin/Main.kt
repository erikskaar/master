import tornadofx.launch
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    checkForExistingBenchmark()
    startFromInput()
    exitProcess(0)
}

fun checkForExistingBenchmark() {
    val file = File("benchmark.csv")
    if (!file.exists()) return
    if (file.readLines().size > 1) {
        var lines = file.readLines()
        var lastLine = lines.last()
        println("A benchmark file was detected. Do you want to continue where it left off? (y/n)")
        when (readlnOrNull()?.trim()?.lowercase() ?: throw IllegalArgumentException("Invalid input")) {
            "n" -> {}
            else -> {
                val points =
                    lastLine.split(",").last().trim().toIntOrNull() ?: throw IOException("Invalid benchmark file")
                while (lastLine.split(",").last().trim().toIntOrNull() == points) {
                    lines = lines.dropLast(1)
                    lastLine = lines.last()
                }
                val difference = (points - lastLine.split(",").last().trim().toIntOrNull()!!)
                val (minPoints, maxPoints, step) = chooseNumberOfPoints(points, difference)
                if (minPoints < points || maxPoints < points) {
                    println("Invalid numbers.")
                    exitProcess(0)
                }
                val times = lastLine.split(",")[6].trim().toIntOrNull() ?: throw IOException("Invalid benchmark file")
                file.writeText(lines.joinToString("\n") + "\n")
                initBenchmark(minPoints, maxPoints, step, BenchmarkType.ALL_QUERIES, Output.APPEND_ONLY, times)
            }
        }
    }
}

fun startFromInput() {
    println("Please select a mode: \n (1) Benchmark \n (2) Visualize")
    when (readlnOrNull()?.trim()?.toIntOrNull() ?: throw IllegalArgumentException("Invalid input")) {
        1 -> {
            val (minPoints, maxPoints, step) = chooseNumberOfPoints()
            val output = chooseOutput()
            val numberOfTimes = chooseNumberOfTimes()
            chooseBenchmarkType(minPoints, maxPoints, step, output, numberOfTimes)
        }

        2 -> launch<RTreeViewApp>()
        3 -> initBenchmark(10_000, 10_000, 10_000, BenchmarkType.ALL_QUERIES, Output.NONE, 1)
        else -> throw IllegalArgumentException("Invalid input.")
    }
}

fun chooseNumberOfTimes(): Int {
    println("Please enter the number of times to run the benchmark: (Default 10)")
    return readlnOrNull()?.toIntOrNull() ?: 10
}

fun chooseNumberOfPoints(preMinPoints: Int = -1, preStep: Int = -1): Triple<Int, Int, Int> {
    var minPoints = preMinPoints
    if (preMinPoints < 0) {
        println("Please enter the minimum number of points: (Default 10_000)")
        minPoints = readlnOrNull()?.toIntOrNull() ?: 10_000
    }
    println("Please enter the max number of points: (Default 500_000)")
    val maxPoints = readlnOrNull()?.toIntOrNull() ?: 500_000
    var step = preStep
    if (preStep < 0) {
        println("Please enter the step count: (Default 10_000)")
        step = readlnOrNull()?.toIntOrNull() ?: 10_000
    }
    return Triple(minPoints, maxPoints, step)
}

fun chooseOutput(): Output {
    println("Choose an output: \n (1) Console \n (2) File \n (3) None")
    return when (readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("Invalid input")) {
        1 -> Output.CONSOLE
        2 -> Output.FILE
        3 -> Output.NONE
        else -> throw IllegalArgumentException("Invalid input.")
    }
}

fun chooseBenchmarkType(minPoints: Int, maxPoints: Int, step: Int, output: Output, times: Int) {
    println(
        "Choose a benchmark type: \n " +
                "(1) Tree creation \n " +
                "(2) Range search \n " +
                "(3) All topological queries \n " +
                "(4) Only index and linked queries \n " +
                "(5) Only linked queries \n " +
                "(6) Only index queries \n " +
                "(7) Only iterative queries \n "
    )
    when (readlnOrNull()?.toIntOrNull() ?: throw IllegalArgumentException("Invalid input")) {
        1 -> initBenchmark(minPoints, maxPoints, step, BenchmarkType.TREE_CREATION, output, times)
        2 -> initBenchmark(minPoints, maxPoints, step, BenchmarkType.RANGE_SEARCH, output, times)
        3 -> initBenchmark(minPoints, maxPoints, step, BenchmarkType.ALL_QUERIES, output, times)
        4 -> initBenchmark(minPoints, maxPoints, step, BenchmarkType.LINKED_LIST_AND_INDEX_QUERY, output, times)
        5 -> initBenchmark(minPoints, maxPoints, step, BenchmarkType.LINKED_LIST_QUERY, output, times)
        6 -> initBenchmark(minPoints, maxPoints, step, BenchmarkType.INDEX_QUERY, output, times)
        7 -> initBenchmark(minPoints, maxPoints, step, BenchmarkType.ITERATIVE_QUERY, output, times)
        else -> throw IllegalArgumentException("Invalid input.")
    }
}

fun initBenchmark(
    minPoints: Int = 10_000,
    maxPoints: Int = 500_000,
    stepValue: Int = 10_000,
    type: BenchmarkType = BenchmarkType.ALL_QUERIES,
    output: Output = Output.CONSOLE,
    times: Int
) {
    if (minPoints > maxPoints) {
        throw IllegalArgumentException("minPoints must be smaller than maxPoints")
    }
    println("Benchmarking from $minPoints to $maxPoints points")

    val TIMES = times
    Benchmarker.output = output
    warmupBenchmark()

    when (type) {
        BenchmarkType.TREE_CREATION -> {
            Benchmarker.benchmarkTreeCreation(maxPoints)
        }

        else -> {
            for (y in 0..10) {
                if (output == Output.FILE) Benchmarker.writeHeadersToFile("benchmark_$y.csv")
                for (x in minPoints..maxPoints step stepValue) {
                    Benchmarker.loadData(x)
                    println("${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}: Started benchmarking $x points. Current run: $y")
                    type.queries.forEach {
                        Benchmarker.benchmarkQuery(
                            it,
                            TIMES,
                            "benchmark_$y.csv",
                            x
                        )
                    }
                    println("${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}: Finished benchmarking $x points")
                    println("--------------------------------------------------\n\n")
                }
                if (output == Output.FILE || output == Output.APPEND_ONLY) println("Benchmark results can be found in benchmark.csv")
            }
        }
    }
}

fun warmupBenchmark() {
    println("Warming up the benchmark...")
    for (x in 10_000..50_000 step 10_000) {
        Benchmarker.loadData(x)
        BenchmarkType.ALL_QUERIES.queries.forEach {
            Benchmarker.benchmarkQuery(it, 10, "warmup.csv", x)
        }
    }

    val file = File("warmup.csv")
    file.delete()
}
