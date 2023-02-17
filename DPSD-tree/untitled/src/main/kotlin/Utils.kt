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

    fun getNorthWestAndSouthEastOfRegionsWithPoints(regions: ArrayList<Region>): Pair<Point, Point> {
        val points = regions.flatMap { it.getPoints().map { node ->  node.getPoint() } }
        return getNorthWestAndSouthEast(points as ArrayList<Point>)
    }

    fun getNorthWestAndSouthEastOfRegionsWithoutPoints(regions: ArrayList<Region>): Pair<Point, Point> {
        val northWest = Point(regions.minOf { it.northWest.x }, regions.maxOf { it.northWest.y })
        val southEast = Point(regions.maxOf { it.southEast.x }, regions.minOf { it.southEast.y })
        return Pair(northWest, southEast)
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
}