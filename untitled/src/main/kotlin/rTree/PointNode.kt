package rTree

import java.util.*
import kotlin.math.abs

class PointNode(
    val coordinates: Point, var prev: PointNode? = null, var next: PointNode? = null, val trajectoryId: String
) {
    private val id = UUID.randomUUID()

    fun getId(): UUID = id

    /**
     * Returns the distance between this point and the given point
     */
    fun getDistanceToPoint(point: PointNode) =
        abs(coordinates.x - point.coordinates.x) + abs(coordinates.y - point.coordinates.y)

    /**
     * Returns the coordinates of the point as a rTree.Point object
     */
    fun getPoint() = Point(coordinates.x, coordinates.y)

    fun getHead(): PointNode {
        var current = this
        while (current.prev !== null) {
            current = current.prev!!
        }
        return current
    }

    fun getTail(): PointNode {
        var current = this
        while (current.next !== null) {
            current = current.next!!
        }
        return current
    }
}