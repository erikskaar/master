package rTree

import Point
import java.util.UUID
import kotlin.math.abs

class PointNode(
    val coordinates: Point, val prev: PointNode? = null, val next: PointNode? = null, val trajectoryId: String
) {
    private val id = UUID.randomUUID()

    fun getId() = id

    fun getDistanceToPoint(point: PointNode) = abs(coordinates.x - point.coordinates.x) + abs(coordinates.y - point.coordinates.y)

    fun getPoint() = Point(coordinates.x, coordinates.y)

}