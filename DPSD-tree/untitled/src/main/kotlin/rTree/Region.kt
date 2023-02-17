package rTree

import Point
import Utils
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

class Region(
    var northWest: Point,
    var southEast: Point,
    var parent: Region? = null,
    private val maxFill: Int = 9,
    private val tree: Tree,
    private val isRoot: Boolean = false
) {
    private val FANOUT = 4
    private val minFill = floor(maxFill * 0.4)

    private val subRegions: ArrayList<Region> = arrayListOf()
    private var points: ArrayList<PointNode> = arrayListOf()

    fun getIsLeaf(): Boolean {
        return subRegions.size == 0
    }

    fun getIsRoot() = isRoot

    fun insert(point: PointNode) {
        if (getIsLeaf()) {
            points.add(point)
            if (points.size > maxFill) {
                split()
            }
        } else {
            val region = getRegionWithSmallestAreaIncreaseRequired(point.getPoint())
            region?.insert(point)
        }
        updateRegionSize()
    }

    fun shouldSplit(): Boolean {
        return when (getIsLeaf()) {
            true -> {
                points.size > maxFill
            }
            false -> {
                (subRegions.size > FANOUT)
            }
        }
    }

    fun splitNonLeafWithParent(
        left: ArrayList<Region>,
        right: ArrayList<Region>,
        leftRegion: Region,
        rightRegion: Region
    ) {
        left.forEach { leftRegion.addSubRegion(it) }
        leftRegion.updateRegionSize()
        right.forEach { rightRegion.addSubRegion(it) }
        rightRegion.updateRegionSize()
        parent?.addSubRegion(leftRegion)
        parent?.addSubRegion(rightRegion)
        parent?.removeSubRegion(this)
        parent?.split()
    }

    fun splitNonLeafRoot(left: ArrayList<Region>, right: ArrayList<Region>, leftRegion: Region, rightRegion: Region) {
        left.forEach { leftRegion.addSubRegion(it) }
        leftRegion.updateRegionSize()
        right.forEach { rightRegion.addSubRegion(it) }
        rightRegion.updateRegionSize()
        tree.updateRootRegions(leftRegion, rightRegion)
    }

    fun split() {
        if (!shouldSplit()) return
        when (getIsLeaf()) {
            false -> {
                subRegions.sortBy { it.getEpicenter().x }
                val median = subRegions.size / 2 + 1
                val left = subRegions.subList(0, median).toMutableList() as ArrayList<Region>
                val right = subRegions.subList(median, subRegions.size).toMutableList() as ArrayList<Region>
                val (leftNorthWest, leftSouthEast) = Utils.getNorthWestAndSouthEastOfRegionsWithoutPoints(left)
                val leftRegion = Region(leftNorthWest, leftSouthEast, tree = tree)
                val (rightNorthWest, rightSouthEast) = Utils.getNorthWestAndSouthEastOfRegionsWithoutPoints(right)
                val rightRegion = Region(rightNorthWest, rightSouthEast, tree = tree)
                if (hasParent()) {
                    splitNonLeafWithParent(left, right, leftRegion, rightRegion)
                } else {
                    splitNonLeafRoot(left, right, leftRegion, rightRegion)
                }
            }
            true -> {
                val pair = Utils.getPointsWithHighestDistance(points)
                val left = pair.first
                val right = pair.second
                val leftRegion = Region(left.getPoint(), left.getPoint(), tree = tree)
                val rightRegion = Region(right.getPoint(), right.getPoint(), tree = tree)
                leftRegion.addPointDirectly(left)
                rightRegion.addPointDirectly(right)
                points.remove(left)
                points.remove(right)
                val pointsCounter = 0
                points.forEach {
                    if (leftRegion.getPoints().size < minFill && (points.size - pointsCounter > minFill - leftRegion.getPoints().size)) {
                        leftRegion.addPointDirectly(it)
                        return@forEach
                    } else if (rightRegion.getPoints().size < minFill && (points.size - pointsCounter > minFill - rightRegion.getPoints().size)) {
                        rightRegion.addPointDirectly(it)
                        return@forEach
                    }
                    if (leftRegion.getAreaIncreaseRequired(it.getPoint()) < rightRegion.getAreaIncreaseRequired(it.getPoint())) {
                        leftRegion.addPointDirectly(it)
                    } else {
                        rightRegion.addPointDirectly(it)
                    }
                    leftRegion.updateRegionSize()
                    rightRegion.updateRegionSize()
                }
                points.clear()
                if (hasParent()) {
                    parent?.addSubRegion(leftRegion)
                    parent?.addSubRegion(rightRegion)
                    parent?.removeSubRegion(this)
                    parent?.split()
                } else {
                    tree.updateRootRegions(leftRegion, rightRegion)
                }
            }
        }
    }

    private fun getAreaIncreaseRequired(point: Point): Double {
        if (doesCoverPoint(point)) return 0.0
        var currentNorthWest = northWest
        var currentSouthEast = southEast

        if (point.x < currentNorthWest.x) {
            currentNorthWest = Point(point.x, currentNorthWest.y)
        }
        if (point.y > currentNorthWest.y) {
            currentNorthWest = Point(currentNorthWest.x, point.y)
        }
        if (point.x > currentSouthEast.x) {
            currentSouthEast = Point(point.x, currentSouthEast.y)
        }
        if (point.y < currentSouthEast.y) {
            currentSouthEast = Point(currentSouthEast.x, point.y)
        }
        val newArea =
            abs(currentNorthWest.x - currentSouthEast.x) * 2 + abs(currentNorthWest.y - currentSouthEast.y) * 2
        return newArea - getArea()
    }

    private fun getRegionWithSmallestAreaIncreaseRequired(point: Point): Region? {
        if (subRegions.isEmpty()) return null
        if (subRegions.any { it.doesCoverPoint(point) }) return subRegions.find { it.doesCoverPoint(point) }
        var result = subRegions.first()
        var smallestAreaIncrease = Double.MAX_VALUE
        subRegions.forEach {
            if (it.getAreaIncreaseRequired(point) < smallestAreaIncrease) {
                result = it
                smallestAreaIncrease = it.getAreaIncreaseRequired(point)
            }
        }
        return result
    }

    private fun clearPoints() {
        points = arrayListOf()
    }

    fun updateRegionSize() {
        if (subRegions.size > 0) {
            northWest = Point(subRegions.minOf { it.northWest.x }, subRegions.maxOf { it.northWest.y })
            southEast = Point(subRegions.maxOf { it.southEast.x }, subRegions.minOf { it.southEast.y })
        } else if (points.size > 0) {
            northWest = Point(points.minOf { it.coordinates.x }, points.maxOf { it.coordinates.y })
            southEast = Point(points.maxOf { it.coordinates.x }, points.minOf { it.coordinates.y })
        } else {
            northWest = parent?.northWest ?: northWest
            southEast = parent?.southEast ?: southEast
        }
    }


    fun getArea() = abs(northWest.x - southEast.x) * 2 + abs(northWest.y - southEast.y) * 2

    fun hasParent(): Boolean = parent !== null

    fun addPointDirectly(point: PointNode) {
        points.add(point)
    }

    private fun doesCoverPoint(point: Point): Boolean {
        return point.x >= northWest.x && point.x <= southEast.x && point.y >= southEast.y && point.y <= northWest.y
    }

    fun addSubRegion(region: Region): Boolean {
        subRegions.add(region)
        region.parent = this
        return true
    }

    private fun removeSubRegion(region: Region) {
        subRegions.remove(region)
    }

    fun getAllSubRegions(regions: ArrayList<Region>, depth: Int = 0): ArrayList<Region> {
        regions.add(this)
        subRegions.forEach { it.getAllSubRegions(regions, depth + 1) }
        if (getIsLeaf()) println("number of points: ${getPoints().size}, depth: $depth") else println("number of subregions: ${getDirectSubRegions().size}, depth: $depth")
        return regions
    }

    fun getDirectSubRegions() = subRegions

    fun getPoints(): ArrayList<PointNode> = points

    fun getEpicenter(): Point {
        val x = abs(northWest.x - southEast.x) / 2
        val y = abs(northWest.y - southEast.y) / 2
        return Point(x, y)
    }

    override fun toString(): String {
        return "[$northWest, $southEast]"
    }
}