package custom

import Point
import kotlin.math.abs

data class BoundingRectangle(val northWest: Point, val southEast: Point) {
    override fun toString() = "NW: $northWest, SE: $southEast\n"

    fun getArea() = abs(northWest.x - southEast.x) * abs(northWest.y - southEast.y)
}