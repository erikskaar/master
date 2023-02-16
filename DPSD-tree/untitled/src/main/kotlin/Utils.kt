import rTree.PointNode
import rTree.Region
import rTree.Tree

object Utils {

    fun getNorthWestAndSouthEast(points: ArrayList<Point>): Pair<Point, Point> {
        return Pair(
            Point(points.minOf { it.x }, points.maxOf { it.y }),
            Point(points.maxOf { it.x }, points.minOf { it.y })
        )
    }

    fun getNorthWestAndSouthEastOfRegions(regions: ArrayList<Region>): Pair<Point, Point> {
        val points = regions.flatMap { it.getPoints().map { node ->  node.getPoint() } }
        return getNorthWestAndSouthEast(points as ArrayList<Point>)
    }

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

    fun getMaxDepth(tree: Tree?): Int = tree?.getMaxDepth() ?: 3
}