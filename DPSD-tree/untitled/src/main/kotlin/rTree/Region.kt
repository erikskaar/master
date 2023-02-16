package rTree

import Point
import Utils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.floor

class Region(
    var northWest: Point,
    var southEast: Point,
    var parent: Region? = null,
    private val maxFill: Int = 9,
    private val depth: Int = 0,
    val tree: Tree? = parent?.tree
) {
    private val FANOUT = 4
    private val minFill = floor(maxFill * 0.4)
    private val id = UUID.randomUUID()

    private val subRegions: ArrayList<Region> = arrayListOf()
    private var points: ArrayList<PointNode> = arrayListOf()
    private var isLeaf = true

    fun getDepth(): Int {
        return depth
    }

    private fun setIsLeaf(new: Boolean) {
        isLeaf = new
    }

    fun getIsLeaf() = isLeaf

    fun balance() {
        if (isLeaf) return
        if (subRegions.all { !it.isLeaf }) return
        if (subRegions.all { it.isLeaf }) return

        val leaves = subRegions.filter { it.isLeaf } as ArrayList<Region>
        val pair = Utils.getNorthWestAndSouthEastOfRegions(leaves)

        val newRegion = Region(pair.first, pair.second, depth = depth + 1)

        subRegions.removeAll(leaves.toSet())
        leaves.forEach { newRegion.addSubRegion(it) }
        addSubRegion(newRegion)
        newRegion.updateRegionSize()
        updateRegionSize()
        parent?.balance()
    }

    private fun splitRegionAtSameLevel() {
        val maxDistancePoints = Utils.getPointsWithHighestDistance(points)
        if (hasParent() && parent?.getDirectSubRegions()?.size!! < FANOUT) {
            val newRegion =
                Region(maxDistancePoints.second.getPoint(), maxDistancePoints.second.getPoint(), depth = depth + 1)
            newRegion.addPointDirectly(maxDistancePoints.second)
            points.remove(maxDistancePoints.second)
            points.remove(maxDistancePoints.first)
            var tempRegion: Region? = Region(maxDistancePoints.first.getPoint(), maxDistancePoints.first.getPoint())
            tempRegion?.addPointDirectly(maxDistancePoints.first)

            points.forEach {
                val minimumAreaIncreaseOld = tempRegion?.getAreaIncreaseRequired(it.getPoint())
                val minimumAreaIncreaseNew = newRegion.getAreaIncreaseRequired(it.getPoint())
                if (minimumAreaIncreaseOld!! <= minimumAreaIncreaseNew) {
                    tempRegion?.addPointDirectly(it)
                } else {
                    newRegion.addPointDirectly(it)
                }
            }
            points = tempRegion?.getPoints()!!
            tempRegion = null

            while (getPoints().size < minFill) {
                var smallestAreaIncrease = Double.MAX_VALUE
                var smallestAreaIncreaseNode: PointNode? = null
                newRegion.getPoints().forEach {
                    val currentSmallestIncrease = getAreaIncreaseRequired(it.getPoint())
                    if (currentSmallestIncrease < smallestAreaIncrease) {
                        smallestAreaIncreaseNode = it
                        smallestAreaIncrease = currentSmallestIncrease
                    }
                }
                if (smallestAreaIncreaseNode !== null) {
                    addPointDirectly(smallestAreaIncreaseNode!!)
                    newRegion.points.remove(smallestAreaIncreaseNode)
                }
            }
            while (newRegion.getPoints().size < minFill) {
                var smallestAreaIncrease = Double.MAX_VALUE
                var smallestAreaIncreaseNode: PointNode? = null
                getPoints().forEach {
                    val currentSmallestIncrease = newRegion.getAreaIncreaseRequired(it.getPoint())
                    if (currentSmallestIncrease < smallestAreaIncrease) {
                        smallestAreaIncreaseNode = it
                        smallestAreaIncrease = currentSmallestIncrease
                    }
                }
                if (smallestAreaIncreaseNode !== null) {
                    newRegion.addPointDirectly(smallestAreaIncreaseNode!!)
                    points.remove(smallestAreaIncreaseNode)
                }
            }
            addSideRegion(newRegion)
            newRegion.updateRegionSize()
            updateRegionSize()
        } else {
            val newRegion = Region(northWest, southEast)
            setIsLeaf(false)
            addSubRegion(newRegion)
            points.forEach { newRegion.addPoint(it) }
            points.clear()
        }
    }

    fun addPoint(point: PointNode) {
        if (isLeaf) {
            points.add(point)
            if (points.size > maxFill) {
                splitRegionAtSameLevel()
            }
        } else {
            getRegionWithSmallestAreaIncreaseRequired(point.getPoint())?.addPoint(point) ?: println("no subregions")
        }
        updateRegionSize()
        balance()
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

    private fun updateRegionSize() {
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

    private fun doesCoverRegion(region: Region): Boolean {
        return region.northWest.x > northWest.x && region.northWest.x < southEast.x &&
                region.southEast.x > northWest.x && region.southEast.x < southEast.x &&
                region.northWest.y > southEast.y && region.northWest.y < northWest.y &&
                region.southEast.y > southEast.y && region.southEast.y < northWest.y
    }

    private fun doesCoverPoint(point: Point): Boolean {
        return point.x >= northWest.x && point.x <= southEast.x && point.y >= southEast.y && point.y <= northWest.y
    }

    private fun addSubRegion(region: Region): Boolean {
        subRegions.add(region)
        region.parent = this
        setIsLeaf(false)
        return true
    }

    private fun removeSubRegion(region: Region) {
        subRegions.remove(region)
    }

    private fun addSideRegion(region: Region) {
        parent?.addSubRegion(region)
    }

    fun getAllSubRegions(regions: ArrayList<Region>, depth: Int = 0): ArrayList<Region> {
        regions.add(this)
        subRegions.forEach { it.getAllSubRegions(regions, depth + 1) }
        if (isLeaf) println("number of points: ${getPoints().size}, depth: $depth") else println("number of subregions: ${getDirectSubRegions().size}, depth: $depth")
        return regions
    }

    fun getDirectSubRegions() = subRegions

    fun getPoints(): ArrayList<PointNode> = points

    override fun toString(): String {
        return "[$northWest, $southEast]"
    }
}