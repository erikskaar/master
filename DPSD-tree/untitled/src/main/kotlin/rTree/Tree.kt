package rTree

import Point
import Utils

class Tree(private val points: ArrayList<PointNode>) {

    private val northWest = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).first
    private val southEast = Utils.getNorthWestAndSouthEast(points.map { it.getPoint() } as ArrayList<Point>).second
    private val rootRegions = arrayListOf<Region>()

    fun updateRootRegions(vararg regions: Region) {
        rootRegions.clear()
        rootRegions.addAll(regions)

        if (rootRegions.size > 1) {
            val (newNorthWest, newSouthEast) = Utils.getNorthWestAndSouthEastOfRegionsWithoutPoints(rootRegions)
            val newRegion = Region(newNorthWest, newSouthEast, tree = this, isRoot = true)
            rootRegions.forEach {  newRegion.addSubRegion(it) }
            rootRegions.clear()
            rootRegions.add(newRegion)
        }
        root = rootRegions.first()
        root.updateRegionSize()
    }

    var root = Region(northWest, southEast, maxFill = 9, tree = this, isRoot = true)

    private fun buildTree() {
        updateRootRegions(regions = arrayOf(root))
        points.forEach {
            root.insert(it)
        }
        println("points: ${getAllPoints().size}")
    }

    fun getAllPoints(): ArrayList<PointNode> = points

    fun getAllRegions(): ArrayList<Region> {
        return root.getAllSubRegions(arrayListOf(root))
    }

    init {
        buildTree()
    }
}