import data.BoundingRectangle
import data.Point
import java.lang.Double.max
import java.lang.Double.min

class Node(private val parent: Node? = null,
    private val x: Double,
    private val y: Double,
    var MBR: BoundingRectangle) {

    init {
        createMBR()
        setParentNodeMBR()
    }

    private fun createMBR() {
        MBR = BoundingRectangle(Point(x - 0.5, y - 0.5), Point(x + 0.5, y + 0.5))
    }

    private fun setParentNodeMBR() {
        if (parent !== null) {
            parent.MBR = BoundingRectangle(
                Point(min(MBR.northWest.x, parent.MBR.northWest.x), min(MBR.northWest.y, parent.MBR.northWest.y)),
                Point(max(MBR.southEast.x, parent.MBR.southEast.x), max(MBR.southEast.y, parent.MBR.southEast.y)),
            )
        }
    }
}