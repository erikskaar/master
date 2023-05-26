package rTree

import Output
import Search
import Utils
import Utils.getHeadsFromIndex
import Utils.getTailsFromIndex
import Variant
import kotlinx.coroutines.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

class Tree(private val points: ArrayList<PointNode>) {

    private val northWest = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).first
    private val southEast = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).second
    private val rootRegions = arrayListOf<Region>()
    private lateinit var index: HashMap<String, Int>

    /**
     * Updates the root region of the tree. Handles the case where the root region is split into multiple regions
     */
    fun updateRootRegions(vararg regions: Region) {
        rootRegions.clear()
        rootRegions.addAll(regions)

        if (rootRegions.size > 1) {
            val (newNorthWest, newSouthEast) = Utils.getNorthWestAndSouthEastOfRegionsWithoutPoints(rootRegions)
            val newRegion = Region(newNorthWest, newSouthEast, tree = this, isRoot = true)
            rootRegions.forEach { newRegion.addSubRegion(it) }
            rootRegions.clear()
            rootRegions.add(newRegion)
        }
        root = rootRegions.first()
        root.updateRegionSize()
    }

    private var root = Region(northWest, southEast, maxFill = 9, tree = this, isRoot = true)

    /**
     * Builds the R-Tree by inserting all points into the root region
     */
    private fun buildTree(): Double {
        // println("Building tree...")
        val start = System.nanoTime()
        updateRootRegions(regions = arrayOf(root))
        points.forEach {
            root.insert(it)
        }
        val end = System.nanoTime()
        println("Tree built in ${((end - start) / 1000000)} ms")
        return (end - start) / 1000000.toDouble()
    }

    /**
     * Returns all the points in the initial list
     */
    fun getAllPoints(): ArrayList<PointNode> = points

    /**
     * Returns all regions in the tree
     */
    fun getAllRegions(): ArrayList<Region> {
        return root.getAllSubRegions(arrayListOf(root))
    }

    fun getDepth(): Int {
        return root.getDepth()
    }

    fun getLeafNodes(): ArrayList<Region> {
        return root.getLeafNodes(arrayListOf())
    }

    /**
     * Returns all points that are in the given region using the R-Tree
     */
    fun rangeSearch(region: Region): ArrayList<PointNode> {
        return root.regionSearch(region, arrayListOf())
    }

    /**
     * Returns every trajectory that starts and ends in the given regions
     */
    fun trajectoryStartAndEndSearch(startRegion: Region, endRegion: Region, type: Variant): List<String> {
        val firstMatches = rangeSearch(startRegion).distinctBy { it.trajectoryId }
        when (type) {
            Variant.LINKED_LIST -> {
                val heads = firstMatches.filter { it.prev == null }
                return heads.filter { endRegion.doesCoverPoint(it.getTail().getPoint()) }.map { it.trajectoryId }
            }

            Variant.ITERATIVE -> {
                val secondMatches = rangeSearch(endRegion).distinctBy { it.trajectoryId }
                val heads = firstMatches.mapNotNull {
                    points.find { head -> head.trajectoryId == it.trajectoryId }
                }
                val tails = secondMatches.mapNotNull {
                    points.findLast { secondFind -> secondFind.trajectoryId == it.trajectoryId }
                }
                val pairs = Utils.matchHeadsAndTailsOfDifferentRegions(heads, tails)
                return pairs.filter { startRegion.doesCoverPoint(it.key.getPoint()) && endRegion.doesCoverPoint(it.value.getPoint()) }
                    .map { it.key.trajectoryId }
            }

            Variant.INDEX -> {
                val secondMatches = rangeSearch(endRegion).distinctBy { it.trajectoryId }
                val heads = getHeadsFromIndex(firstMatches, index, points)
                val tails = getTailsFromIndex(secondMatches, index, points)
                val pairs = Utils.matchHeadsAndTailsOfDifferentRegions(heads, tails)
                return pairs.filter { startRegion.doesCoverPoint(it.key.getPoint()) && endRegion.doesCoverPoint(it.value.getPoint()) }
                    .map { it.key.trajectoryId }
            }
        }
    }

    /**
     * Returns every trajectory passes through the given region
     */
    fun passesThroughSearch(region: Region, type: Variant): List<String> {
        val matches = rangeSearch(region).distinctBy { it.trajectoryId }
        when (type) {
            Variant.LINKED_LIST -> return matches.filter {
                !region.doesCoverPoint(it.getHead().getPoint()) && !region.doesCoverPoint(
                    it.getTail().getPoint()
                )
            }.map { it.trajectoryId }.distinct()

        Variant.ITERATIVE -> {
            val heads = matches.distinctBy { it.trajectoryId }.mapNotNull { point ->
                points.find { it.trajectoryId == point.trajectoryId }
            }
            val tails = matches.distinctBy { it.trajectoryId }.mapNotNull { point ->
                points.findLast { it.trajectoryId == point.trajectoryId }
            }
            val pairs = Utils.matchHeadsAndTails(heads, tails)
            return pairs.filter { !region.doesCoverPoint(it.key.getPoint()) && !region.doesCoverPoint(it.value.getPoint()) }
                .map { it.key.trajectoryId }
        }

        Variant.INDEX -> {
            val heads = getHeadsFromIndex(matches, index, points)
            val tails = getTailsFromIndex(matches, index, points)
            val pairs = Utils.matchHeadsAndTails(heads, tails)
            return pairs.filter { !region.doesCoverPoint(it.key.getPoint()) && !region.doesCoverPoint(it.value.getPoint()) }
                .map { it.key.trajectoryId }
        }
    }
}

/**
 * Returns every trajectory that enters the given region, but does not leave it
 */
fun entersSearch(region: Region, type: Variant): List<String> {
    val matches = rangeSearch(region).distinctBy { it.trajectoryId }
    when (type) {
        Variant.LINKED_LIST -> return matches.filter {
            !region.doesCoverPoint(it.getHead().getPoint()) && region.doesCoverPoint(it.getTail().getPoint())
        }.map { it.trajectoryId }

        Variant.ITERATIVE -> {
            val heads = matches.mapNotNull { point ->
                points.find { it.trajectoryId == point.trajectoryId }
            }
            val tails = matches.mapNotNull { point ->
                points.findLast { it.trajectoryId == point.trajectoryId }
            }
            val pairs =
                heads.associateWith { head -> tails.first { head.trajectoryId == it.trajectoryId } }
            return pairs.filter { !region.doesCoverPoint(it.key.getPoint()) && region.doesCoverPoint(it.value.getPoint()) }
                .map { it.key.trajectoryId }.distinct()
        }

        Variant.INDEX -> {
            val heads = getHeadsFromIndex(matches, index, points)
            val tails = getTailsFromIndex(matches, index, points)
            val pairs = Utils.matchHeadsAndTails(heads, tails)
            return pairs.filter {
                !region.doesCoverPoint(it.key.getPoint()) &&
                        region.doesCoverPoint(it.value.getPoint())
            }.map { it.key.trajectoryId }
        }
    }
}

/**
 * Returns every trajectory that leaves the given region, but does not enter it
 */
fun leavesSearch(region: Region, type: Variant): List<String> {
    val matches = rangeSearch(region).distinctBy { it.trajectoryId }
    when (type) {
        Variant.LINKED_LIST -> return matches.filter {
            region.doesCoverPoint(it.getHead().getPoint()) && !region.doesCoverPoint(it.getTail().getPoint())
        }.map { it.trajectoryId }

        Variant.ITERATIVE -> {
            val heads = matches.mapNotNull { point ->
                points.find { it.trajectoryId == point.trajectoryId }
            }
            val tails = matches.mapNotNull { point ->
                points.findLast { it.trajectoryId == point.trajectoryId }
            }
            val pairs = Utils.matchHeadsAndTails(heads, tails)
            return pairs.filter { region.doesCoverPoint(it.key.getPoint()) && !region.doesCoverPoint(it.value.getPoint()) }
                .map { it.key.trajectoryId }.distinct()
        }

        Variant.INDEX -> {
            val heads = getHeadsFromIndex(matches, index, points)
            val tails = getTailsFromIndex(matches, index, points)
            val pairs = Utils.matchHeadsAndTails(heads, tails)
            return pairs.filter { region.doesCoverPoint(it.key.getPoint()) && !region.doesCoverPoint(it.value.getPoint()) }
                .map { it.key.trajectoryId }.distinct()
        }
    }
}

/**
 * Method for timing the search methods
 */
private fun timeSearchMethodAverage(
    method: () -> Unit,
    times: Int,
    name: String,
    output: Output,
    fileName: String,
    numberOfPoints: Int
) {
    val threadPool = Executors.newFixedThreadPool(times).asCoroutineDispatcher()
    val speeds = ConcurrentLinkedQueue<Double>()
    runBlocking(threadPool) {
        val jobs = mutableListOf<Deferred<Unit>>()
        for (x in 0 until times) {
            jobs += async {
                val start = System.nanoTime()
                method()
                val end = System.nanoTime()
                val time = ((end - start) / 1000000).toDouble()
                speeds.add(time)
            } as Deferred<Unit>
        }
        jobs.joinAll()
        val nonNullSpeeds = speeds.sorted()
        val median: Double = Utils.calculateMedian(nonNullSpeeds)
        when (output) {
            Output.CONSOLE -> {
                println("---------------------------")
                println("'$name':")
                println("Speeds: ${nonNullSpeeds.map { "$it ms" }}")
                println("Average: ${nonNullSpeeds.average()} ms")
                println("Median: $median ms")
                println("Max: ${nonNullSpeeds.maxOf { it }} ms")
                println("Min: ${nonNullSpeeds.minOf { it }} ms")
            }

            Output.FILE -> {
                Utils.writeOutput(
                    "${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}, '$name', ${nonNullSpeeds.average()}, $median, ${nonNullSpeeds.maxOf { it }}, ${nonNullSpeeds.minOf { it }}, ${speeds.size}, $numberOfPoints\n",
                    fileName
                )
            }
            Output.APPEND_ONLY -> {
                Utils.writeOutput(
                    "${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}, '$name', ${nonNullSpeeds.average()}, $median, ${nonNullSpeeds.maxOf { it }}, ${nonNullSpeeds.minOf { it }}, ${speeds.size}, $numberOfPoints\n",
                    fileName
                )
            }

            Output.NONE -> {}
        }
    }
    threadPool.close()
}

fun speedCheck(type: Search, times: Int, output: Output, fileName: String, numberOfPoints: Int) {
    val region = Region(Point(0.385, 0.865), Point(0.395, 0.850), tree = this)
    val endRegion = Region(Point(0.370, 0.845), Point(0.380, 0.830), tree = this)
    when (type) {
        Search.TRAJECTORY_START_AND_END_REGION_SEARCH -> timeSearchMethodAverage(
            { (::trajectoryStartAndEndSearch)(region, endRegion, Variant.ITERATIVE) },
            times,
            Search.TRAJECTORY_START_AND_END_REGION_SEARCH.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_START_AND_END_REGION_SEARCH_WITH_LINKS -> timeSearchMethodAverage(
            { (::trajectoryStartAndEndSearch)(region, region, Variant.LINKED_LIST) },
            times,
            Search.TRAJECTORY_START_AND_END_REGION_SEARCH_WITH_LINKS.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_START_AND_END_REGION_SEARCH_WITH_INDEX -> timeSearchMethodAverage(
            { (::trajectoryStartAndEndSearch)(region, endRegion, Variant.INDEX) },
            times,
            Search.TRAJECTORY_START_AND_END_REGION_SEARCH_WITH_INDEX.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_PASSES_THROUGH_REGION_SEARCH -> timeSearchMethodAverage(
            { (::passesThroughSearch)(region, Variant.ITERATIVE) },
            times,
            Search.TRAJECTORY_PASSES_THROUGH_REGION_SEARCH.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_PASSES_THROUGH_REGION_SEARCH_WITH_LINKS -> timeSearchMethodAverage(
            { (::passesThroughSearch)(region, Variant.LINKED_LIST) },
            times,
            Search.TRAJECTORY_PASSES_THROUGH_REGION_SEARCH_WITH_LINKS.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_PASSES_THROUGH_REGION_SEARCH_WITH_INDEX -> timeSearchMethodAverage(
            { (::passesThroughSearch)(region, Variant.INDEX) },
            times,
            Search.TRAJECTORY_PASSES_THROUGH_REGION_SEARCH_WITH_INDEX.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_ENTERS_REGION_SEARCH -> timeSearchMethodAverage(
            { (::entersSearch)(region, Variant.ITERATIVE) },
            times,
            Search.TRAJECTORY_ENTERS_REGION_SEARCH.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_ENTERS_REGION_SEARCH_WITH_LINKS -> timeSearchMethodAverage(
            { (::entersSearch)(region, Variant.LINKED_LIST) },
            times,
            Search.TRAJECTORY_ENTERS_REGION_SEARCH_WITH_LINKS.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_ENTERS_REGION_SEARCH_WITH_INDEX -> timeSearchMethodAverage(
            { (::entersSearch)(region, Variant.INDEX) },
            times,
            Search.TRAJECTORY_ENTERS_REGION_SEARCH_WITH_INDEX.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_LEAVES_REGION_SEARCH -> timeSearchMethodAverage(
            { (::leavesSearch)(region, Variant.ITERATIVE) },
            times,
            Search.TRAJECTORY_LEAVES_REGION_SEARCH.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_LEAVES_REGION_SEARCH_WITH_LINKS -> timeSearchMethodAverage(
            { (::leavesSearch)(region, Variant.LINKED_LIST) },
            times,
            Search.TRAJECTORY_LEAVES_REGION_SEARCH_WITH_LINKS.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.TRAJECTORY_LEAVES_REGION_SEARCH_WITH_INDEX -> timeSearchMethodAverage(
            { (::leavesSearch)(region, Variant.INDEX) },
            times,
            Search.TRAJECTORY_LEAVES_REGION_SEARCH_WITH_INDEX.outputName,
            output,
            fileName,
            numberOfPoints
        )

        Search.RANGE_SEARCH -> timeSearchMethodAverage(
            { (::rangeSearch)(region) },
            times,
            Search.RANGE_SEARCH.outputName,
            output,
            fileName,
            numberOfPoints
        )
    }
}

private fun createIndex() {
    val index = HashMap<String, Int>()
    var previousTrajectoryId = ""
    points.forEachIndexed { i, point ->
        if (point.trajectoryId != previousTrajectoryId) {
            index[point.trajectoryId] = points.indexOf(point)
            previousTrajectoryId = points[i].trajectoryId
        }
    }
    this.index = index
    println("Index created")
}

init {
    buildTree()
    println("Tree has ${getAllPoints().size} points and ${getAllRegions().size} regions")
    createIndex()
}
}