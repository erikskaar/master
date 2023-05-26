import rTree.Point
import rTree.PointNode
import rTree.Region
import tornadofx.c
import java.io.File

object Utils {

    /**
     * Returns the north-west and south-east points of the given points
     */
    fun getNorthWestAndSouthEast(points: ArrayList<Point>): Pair<Point, Point> {
        return Pair(
            Point(points.minOf { it.x }, points.maxOf { it.y }),
            Point(points.maxOf { it.x }, points.minOf { it.y })
        )
    }

    /**
     * Returns the north-west and south-east points of the given regions without the points in the regions
     */
    fun getNorthWestAndSouthEastOfRegionsWithoutPoints(regions: ArrayList<Region>): Pair<Point, Point> {
        val northWest = Point(regions.minOf { it.northWest.x }, regions.maxOf { it.northWest.y })
        val southEast = Point(regions.maxOf { it.southEast.x }, regions.minOf { it.southEast.y })
        return Pair(northWest, southEast)
    }

    /**
     * Returns an array of points that can be used to draw a rectangle for the given region
     * Takes a multiplier to scale the points
     */
    fun getRectanglePointsForRegion(region: Region, multiplier: Int): Array<Number> {
        return arrayOf(
            region.northWest.x * multiplier,
            region.northWest.y * multiplier,

            region.southEast.x * multiplier,
            region.northWest.y * multiplier,

            region.southEast.x * multiplier,
            region.southEast.y * multiplier,

            region.northWest.x * multiplier,
            region.southEast.y * multiplier,

            region.northWest.x * multiplier,
            region.northWest.y * multiplier,
        )
    }

    /**
     * Returns the two points with the highest distance between them
     */
    fun getPointsWithHighestDistance(points: ArrayList<PointNode>): Pair<PointNode, PointNode> {
        var highestDistance = 0.0
        var highestDistancePair: Pair<PointNode, PointNode> = Pair(points.first(), points.first())

        points.forEach { firstNode ->
            points.forEach { secondNode ->
                val distance = firstNode.getDistanceToPoint(secondNode)
                if (distance > highestDistance) {
                    highestDistance = distance
                    highestDistancePair = Pair(firstNode, secondNode)
                }
            }
        }
        return highestDistancePair
    }

    /**
     * Creates links between the points for every trajectory in the given list
     */
    fun createPointNodeLinks(pointNodes: ArrayList<PointNode>): ArrayList<PointNode> {
        // println("Creating links...")
        val start = System.nanoTime()
        val trajectories = arrayListOf<String>()
        var counter = 0
        pointNodes.forEachIndexed { index, it ->
            if (!trajectories.contains(it.trajectoryId)) {
                trajectories.add(it.trajectoryId)
                it.prev = null
                if (index != 0) {
                    pointNodes[index - 1].next = null
                }
            } else {
                it.prev = pointNodes[index - 1]
                pointNodes[index - 1].next = it
            }
        }
        val end = System.nanoTime()
        println("Done creating links for ${trajectories.size} trajectories in ${(end - start) / 1000000} ms")
        return pointNodes
    }

    fun writeOutput(output: String, fileName: String) {
        val file = File(fileName)
        file.appendText(output)
    }

    fun hashToColor(input: String): javafx.scene.paint.Color {
        val hash = input.hashCode()
        val red = (hash and 0xFF) / 255.0f.toDouble()
        val green = ((hash shr 8) and 0xFF) / 255.0f.toDouble()
        val blue = ((hash shr 16) and 0xFF) / 255.0f.toDouble()
        return c(red, green, blue, 1.0)
    }

    fun matchHeadsAndTails(heads: List<PointNode>, tails: List<PointNode>) =
        heads.associateWith { head -> tails.first { head.trajectoryId == it.trajectoryId } }

    fun matchHeadsAndTailsOfDifferentRegions(heads: List<PointNode>, tails: List<PointNode>) =
        heads.associateWith { head -> tails.find { head.trajectoryId == it.trajectoryId } }
            .filter { it.value != null } as Map<PointNode, PointNode>

    /**
     * Gets all tail nodes using the index
     */
    fun getTailsFromIndex(
        matches: List<PointNode>,
        index: HashMap<String, Int>,
        points: ArrayList<PointNode>
    ): List<PointNode> {
        val tails = ArrayList<PointNode>()
        matches.forEach {
            for (x in index[it.trajectoryId]!! until points.size) {
                if (points[x].trajectoryId != it.trajectoryId) {
                    tails.add(points[x - 1])
                    break
                } else if (x == points.size - 1) {
                    tails.add(points[x])
                }
            }
        }
        return tails.distinct()
    }

    fun getHeadsFromIndex(
        matches: List<PointNode>,
        index: HashMap<String, Int>,
        points: ArrayList<PointNode>
    ): List<PointNode> {
        return matches.map { point -> points[index[point.trajectoryId]!!] }.distinct()
    }

    fun calculateMedian(values: List<Double>): Double {
        if (values.isEmpty()) throw IllegalArgumentException("List must contain at least one value")
        val sortedList = values.sorted()
        val middle = sortedList.size / 2
        return if (sortedList.size % 2 == 0) {
            (sortedList[middle - 1] + sortedList[middle]) / 2
        } else {
            sortedList[middle]
        }
    }
}