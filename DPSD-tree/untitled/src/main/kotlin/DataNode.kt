import java.util.*
import kotlin.math.max
import kotlin.math.min

class DataNode(
    private val x: Double, private val y: Double, private var trajectoryId: String = ""
) : Node {
    private var boundingRectangle: BoundingRectangle = BoundingRectangle(
        northWest = Point(x, y), southEast = Point(x, y)
    )
    private val id: UUID = UUID.randomUUID()
    private var parent: Node? = null
    private var child: Node? = null

    override fun setParentMBR() {
        parent?.setBoundingRectangle(
            BoundingRectangle(
                northWest = Point(
                    min(boundingRectangle.northWest.x, parent!!.getBoundingRectangle().northWest.x),
                    max(boundingRectangle.northWest.y, parent!!.getBoundingRectangle().northWest.y),
                ), southEast = Point(
                    max(boundingRectangle.southEast.x, parent!!.getBoundingRectangle().southEast.x),
                    min(boundingRectangle.southEast.y, parent!!.getBoundingRectangle().southEast.y),
                )
            )
        )
    }

    override fun setParent(node: Node?) {
        parent = node
    }

    override fun setChild(node: Node) {
        child = node
    }

    override fun getParent() = parent

    override fun getChild() = child

    override fun setBoundingRectangle(newMBR: BoundingRectangle) {
        boundingRectangle = newMBR
    }

    override fun resetBoundingRectangle() {
        boundingRectangle = BoundingRectangle(Point(x, y), Point(x, y))
    }

    override fun getBoundingRectangle() = boundingRectangle

    override fun getBoundingRectangleArea() = boundingRectangle.getArea()

    override fun getX() = x

    override fun getY() = y

    fun printCoordinates() = "${x}, ${y}\n"

    override fun getId() = id

    fun getParentId() = parent?.getId()

    fun getTrajectoryId() = trajectoryId

    override fun getType(): String = "data"
}