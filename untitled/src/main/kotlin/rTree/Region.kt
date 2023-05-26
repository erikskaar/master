package rTree

import Utils
import kotlin.math.abs
import kotlin.math.floor

class Region(
    var northWest: Point,
    var southEast: Point,
    private var parent: Region? = null,
    private val maxFill: Int = 9,
    private val tree: Tree,
    private val isRoot: Boolean = false
) {
    private val FANOUT = 4
    private val minFill = floor(maxFill * 0.4)

    private val subRegions: ArrayList<Region> = arrayListOf()
    private var points: ArrayList<PointNode> = arrayListOf()

    /**
     * Returns if the node is a leaf node
     */
    fun getIsLeaf(): Boolean {
        return subRegions.size == 0
    }

    /**
     * Returns if the node is the root node
     */
    fun getIsRoot() = isRoot

    /**
     * inserts a point into the region, splitting the region if necessary, or inserting the point into a subregion
     */
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

    /**
     * Returns whether the region should be split due to the maxFill or FANOUT being exceeded
     */
    private fun shouldSplit(): Boolean {
        return when (getIsLeaf()) {
            true -> {
                points.size > maxFill
            }
            false -> {
                (subRegions.size > FANOUT)
            }
        }
    }

    /**
     * Splits the region IF it has a parent into two regions, dividing the subregions between the two regions
     * Propagates the split up the tree if necessary
     */
    private fun splitNonLeafWithParent(
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

    /**
     * Splits the region IF it is the root and is not a leaf node into two regions, dividing the subregions between the two regions
     * Calls updateRootRegions on the tree to update the root region
     */
    private fun splitNonLeafRoot(left: ArrayList<Region>, right: ArrayList<Region>, leftRegion: Region, rightRegion: Region) {
        left.forEach { leftRegion.addSubRegion(it) }
        leftRegion.updateRegionSize()
        right.forEach { rightRegion.addSubRegion(it) }
        rightRegion.updateRegionSize()
        tree.updateRootRegions(leftRegion, rightRegion)
    }

    /**
     * Calls the appropriate split function depending on whether the region is a leaf node or not and if it has a parent
     */
    private fun split() {
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

    /**
     * Returns the increase in area required to cover the point
     */
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

    /**
     * Returns the region with the smallest area increase required to cover the point
     */
    private fun getRegionWithSmallestAreaIncreaseRequired(point: Point): Region? {
        if (subRegions.isEmpty()) return null
        if (subRegions.any { it.doesCoverPoint(point) }) return subRegions.find { it.doesCoverPoint(point) }
        var result = subRegions.first()
        var smallestAreaIncrease = Double.MAX_VALUE
        subRegions.forEach {
            if (it.getAreaIncreaseRequired(point) < smallestAreaIncrease) {
                result = it
                smallestAreaIncrease = it.getAreaIncreaseRequired(point)
            } else if (it.getAreaIncreaseRequired(point) == smallestAreaIncrease) {
                if (it.getArea() < result.getArea()) {
                    result = it
                    smallestAreaIncrease = it.getAreaIncreaseRequired(point)
                }
            }
        }
        return result
    }

    /**
     * Updates the region size based on the subregions and points
     */
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

    /**
     * Returns the circumference of the region
     */
    private fun getArea() = abs(northWest.x - southEast.x) * 2 + abs(northWest.y - southEast.y) * 2

    /**
     * Returns true if the region has a parent
     */
    private fun hasParent(): Boolean = parent !== null

    /**
     * Add point to the region without checking for splitting
     */
    private fun addPointDirectly(point: PointNode) {
        points.add(point)
    }

    /**
     * Returns true if the region covers the point
     */
    fun doesCoverPoint(point: Point): Boolean {
        return point.x >= northWest.x && point.x <= southEast.x && point.y >= southEast.y && point.y <= northWest.y
    }

    /**
     * Adds a subregion to the region, making sure to update the parent
     */
    fun addSubRegion(region: Region) {
        subRegions.add(region)
        region.parent = this
    }

    /**
     * Removes a subregion from the region
     */
    private fun removeSubRegion(region: Region) {
        subRegions.remove(region)
    }

    /**
     * Recursively returns all subregions
     */
    fun getAllSubRegions(regions: ArrayList<Region>, depth: Int = 0): ArrayList<Region> {
        regions.add(this)
        subRegions.forEach { it.getAllSubRegions(regions, depth + 1) }
        return regions
    }

    fun getDepth(): Int {
        return if (getIsLeaf()) {
            0
        } else {
            subRegions.maxOf { it.getDepth() } + 1
        }
    }

    fun getLeafNodes(regions: ArrayList<Region>): ArrayList<Region> {
        if (getIsLeaf()) {
            regions.add(this)
        } else {
            subRegions.forEach { it.getLeafNodes(regions) }
        }
        return regions
    }
    /**
     * Returns the points attached to the region
     */
    private fun getPoints(): ArrayList<PointNode> = points

    /**
     * Returns the center of the region as a point
     */
    private fun getEpicenter(): Point {
        val x = abs(northWest.x - southEast.x) / 2
        val y = abs(northWest.y - southEast.y) / 2
        return Point(x, y)
    }

    /**
     * Searches the region recursively for points that match the region
     */
    fun regionSearch(region: Region, matches: ArrayList<PointNode>): ArrayList<PointNode> {
        if (doesIntersectRegion(region)) {
            if (getIsLeaf()) {
                points.forEach {
                    if (region.doesCoverPoint(it.getPoint())) {
                        matches.add(it)
                    }
                }
            } else {
                subRegions.forEach {
                    if (it.doesIntersectRegion(region)) {
                        it.regionSearch(region, matches)
                    }
                }
            }
        }
        return matches
    }

    /**
     * Returns true if the region covers the region
     */
    fun coversRegion(region: Region): Boolean {
        return !(
                region.southEast.x < northWest.x ||
                        region.northWest.x > southEast.x ||
                        region.northWest.y < southEast.y ||
                        region.southEast.y > northWest.y
                )
    }

    /**
     * Returns true if the region intersects the region
     */
    private fun doesIntersectRegion(region: Region): Boolean {
        return !(region.southEast.x < northWest.x || region.northWest.x > southEast.x || region.northWest.y < southEast.y || region.southEast.y > northWest.y)
    }

    override fun toString(): String {
        return "[$northWest, $southEast]"
    }
}