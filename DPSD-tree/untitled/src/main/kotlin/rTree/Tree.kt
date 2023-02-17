package rTree

import Point
import Utils

class Tree(private val points: ArrayList<PointNode>) {

    private val northWest = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).first
    private val southEast = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).second

    var root = Region(northWest, southEast, maxFill = 9, tree = this)

    val regions = arrayListOf(root)

    private fun buildTree() {
        points.forEach {
            root.addPoint(it)
        }
        println("points: ${getAllPoints().size}")
    }

    fun getAllPoints(): ArrayList<PointNode> = points

    fun getAllRegions(): ArrayList<Region> {
        return root.getAllSubRegions(arrayListOf(root))
    }

    fun getMaxDepth(): Int = 3

    init {
        buildTree()
    }
}