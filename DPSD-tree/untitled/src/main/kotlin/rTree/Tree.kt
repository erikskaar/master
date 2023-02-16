package rTree

import Point
import Utils

class Tree(private val points: ArrayList<PointNode>) {

    private val northWest = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).first
    private val southEast = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).second

    private val root = Region(northWest, southEast, maxFill = 3, tree = this)

    private fun buildTree() {
        points.forEach {
            root.addPoint(it)
        }
        root.balance()
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