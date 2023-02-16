package custom

import java.util.*

interface Node {
    fun setBoundingRectangle(newMBR: BoundingRectangle)
    fun getBoundingRectangle(): BoundingRectangle
    fun getId(): UUID
    fun getBoundingRectangleArea(): Double
    fun getParent(): Node?
    fun setParent(node: Node?)
    fun setParentMBR()
    fun getType(): String
    fun getX(): Double
    fun getY(): Double
    fun setChild(node: Node)
    fun getChild(): Node?
    fun resetBoundingRectangle()
}