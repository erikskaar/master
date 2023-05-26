package rTree

import Utils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TreeTest {
    private val nodes: ArrayList<PointNode> = arrayListOf(
        PointNode(coordinates = Point(1.0, 5.0), trajectoryId = "1"),
        PointNode(coordinates = Point(1.0, 4.0), trajectoryId = "1"),
        PointNode(coordinates = Point(1.0, 3.0), trajectoryId = "1"),

        PointNode(coordinates = Point(5.0, 1.0), trajectoryId = "2"),
        PointNode(coordinates = Point(5.0, 2.0), trajectoryId = "2"),
        PointNode(coordinates = Point(5.0, 3.0), trajectoryId = "2"),

        PointNode(coordinates = Point(2.0, 1.0), trajectoryId = "3"),
        PointNode(coordinates = Point(3.0, 2.0), trajectoryId = "3"),
        PointNode(coordinates = Point(4.0, 3.0), trajectoryId = "3"),
    )

    /**  ____________
     *5 | 1 . . . . |
     *4 | 1 . . . . |
     *3 | 1 . . 3 2 |
     *2 | . . 3 . 2 |
     *1 | . 3 . . 2 |
     *   –––––––––––
     *    1 2 3 4 5
     */

    private var tree = Tree(Utils.createPointNodeLinks(nodes))

    @Test
    fun `Test Tree Range Search`() {
        val result = tree.rangeSearch(Region(Point(x = 4.0, y = 3.0), Point(x = 6.0, y = 1.0), tree = tree))
        assertEquals(4, result.size)
        assertEquals(Point(5.0, 1.0), result[0].coordinates)
    }

    @Test
    fun `Test Tree Range Search 2`() {
        val result = tree.rangeSearch(Region(Point(x = 0.0, y = 6.0), Point(x = 6.0, y = 0.0), tree = tree))
        assertEquals(9, result.size)
        assertEquals(Point(1.0, 5.0), result[0].coordinates)
    }

    @Test
    fun `Test Trajectory Start and End Region Search Without Links`() {
        val result = tree.trajectoryStartAndEndSearch(
            Region(Point(x = 0.0, y = 6.0), Point(x = 6.0, y = 0.0), tree = tree),
            Region(Point(x = 0.0, y = 6.0), Point(x = 6.0, y = 0.0), tree = tree),
            Variant.ITERATIVE
        )
        assertEquals(3, result.size)
    }

    @Test
    fun `Test Trajectory Start and End Region Search With Index`() {
        val result = tree.trajectoryStartAndEndSearch(
            Region(Point(x = 0.0, y = 6.0), Point(x = 6.0, y = 0.0), tree = tree),
            Region(Point(x = 0.0, y = 6.0), Point(x = 6.0, y = 0.0), tree = tree),
            Variant.INDEX
        )
        assertEquals(3, result.size)
    }

    @Test
    fun `Test Trajectory Start and End Region Search Without Links 2`() {
        val result = tree.trajectoryStartAndEndSearch(
            Region(Point(x = 0.0, y = 6.0), Point(x = 2.0, y = 0.0), tree = tree),
            Region(Point(x = 4.0, y = 4.0), Point(x = 6.0, y = 0.0), tree = tree),
            Variant.ITERATIVE
        )
        assertEquals(1, result.size)
    }

    @Test
    fun `Test Trajectory Start and End Region Search With Links`() {
        val result = tree.trajectoryStartAndEndSearch(
            Region(Point(x = 0.0, y = 6.0), Point(x = 6.0, y = 0.0), tree = tree),
            Region(Point(x = 0.0, y = 6.0), Point(x = 6.0, y = 0.0), tree = tree),
            Variant.LINKED_LIST
        )
        assertEquals(3, result.size)
    }

    @Test
    fun `Test Trajectory Start and End Region Search With Links 2`() {
        val result = tree.trajectoryStartAndEndSearch(
            Region(Point(x = 0.0, y = 6.0), Point(x = 2.0, y = 0.0), tree = tree),
            Region(Point(x = 4.0, y = 4.0), Point(x = 6.0, y = 0.0), tree = tree),
            Variant.LINKED_LIST
        )
        assertEquals(1, result.size)
    }

    @Test
    fun `Test Trajectory Passes Through Region Search With Links`() {
        var region = Region(Point(x = 1.0, y = 4.0), Point(x = 1.0, y = 4.0), tree = tree)
        var result = tree.passesThroughSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 3.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.LINKED_LIST)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 2.0), Point(x = 5.0, y = 2.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 3.0, y = 2.0), Point(x = 5.0, y = 2.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.LINKED_LIST)
        assertEquals(2, result.size)
        assertEquals("2", result[0])
        assertEquals("3", result[1])
    }

    @Test
    fun `Test Trajectory Passes Through Region Search Without Links`() {
        var region = Region(Point(x = 1.0, y = 4.0), Point(x = 1.0, y = 4.0), tree = tree)
        var result = tree.passesThroughSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 3.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.ITERATIVE)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 2.0), Point(x = 5.0, y = 2.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 3.0, y = 2.0), Point(x = 5.0, y = 2.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.ITERATIVE)
        assertEquals(2, result.size)
        assertEquals("2", result[0])
        assertEquals("3", result[1])
    }

    @Test
    fun `Test Trajectory Passes Through Region Search With Index`() {
        var region = Region(Point(x = 1.0, y = 4.0), Point(x = 1.0, y = 4.0), tree = tree)
        var result = tree.passesThroughSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 3.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.INDEX)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 2.0), Point(x = 5.0, y = 2.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 3.0, y = 2.0), Point(x = 5.0, y = 2.0), tree = tree)
        result = tree.passesThroughSearch(region, Variant.INDEX)
        assertEquals(2, result.size)
        assertEquals("2", result[0])
        assertEquals("3", result[1])
    }

    @Test
    fun `Test Trajectory Enters Region Search With Links`() {
        var region = Region(Point(x = 1.0, y = 3.0), Point(x = 1.0, y = 3.0), tree = tree)
        var result = tree.entersSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 5.0), tree = tree)
        result = tree.entersSearch(region, Variant.LINKED_LIST)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 3.0), Point(x = 5.0, y = 3.0), tree = tree)
        result = tree.entersSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 4.0, y = 3.0), Point(x = 4.0, y = 3.0), tree = tree)
        result = tree.entersSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("3", result[0])
    }

    @Test
    fun `Test Trajectory Enters Region Search Without Links`() {
        var region = Region(Point(x = 1.0, y = 3.0), Point(x = 1.0, y = 3.0), tree = tree)
        var result = tree.entersSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 5.0), tree = tree)
        result = tree.entersSearch(region, Variant.ITERATIVE)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 3.0), Point(x = 5.0, y = 3.0), tree = tree)
        result = tree.entersSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 4.0, y = 3.0), Point(x = 4.0, y = 3.0), tree = tree)
        result = tree.entersSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("3", result[0])
    }

    @Test
    fun `Test Trajectory Enters Region Search With Index`() {
        var region = Region(Point(x = 1.0, y = 3.0), Point(x = 1.0, y = 3.0), tree = tree)
        var result = tree.entersSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 5.0), tree = tree)
        result = tree.entersSearch(region, Variant.INDEX)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 3.0), Point(x = 5.0, y = 3.0), tree = tree)
        result = tree.entersSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 4.0, y = 3.0), Point(x = 4.0, y = 3.0), tree = tree)
        result = tree.entersSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("3", result[0])
    }

    @Test
    fun `Test Trajectory Leaves Region Search With Links`() {
        var region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 5.0), tree = tree)
        var result = tree.leavesSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 3.0), Point(x = 1.0, y = 3.0), tree = tree)
        result = tree.leavesSearch(region, Variant.LINKED_LIST)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 1.0), Point(x = 5.0, y = 1.0), tree = tree)
        result = tree.leavesSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 2.0, y = 1.0), Point(x = 2.0, y = 1.0), tree = tree)
        result = tree.leavesSearch(region, Variant.LINKED_LIST)
        assertEquals(1, result.size)
        assertEquals("3", result[0])
    }

    @Test
    fun `Test Trajectory Leaves Region Search Without Links`() {
        var region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 5.0), tree = tree)
        var result = tree.leavesSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 3.0), Point(x = 1.0, y = 3.0), tree = tree)
        result = tree.leavesSearch(region, Variant.ITERATIVE)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 1.0), Point(x = 5.0, y = 1.0), tree = tree)
        result = tree.leavesSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 2.0, y = 1.0), Point(x = 2.0, y = 1.0), tree = tree)
        result = tree.leavesSearch(region, Variant.ITERATIVE)
        assertEquals(1, result.size)
        assertEquals("3", result[0])
    }

    @Test
    fun `Test Trajectory Leaves Region Search With Index`() {
        var region = Region(Point(x = 1.0, y = 5.0), Point(x = 1.0, y = 5.0), tree = tree)
        var result = tree.leavesSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("1", result[0])

        region = Region(Point(x = 1.0, y = 3.0), Point(x = 1.0, y = 3.0), tree = tree)
        result = tree.leavesSearch(region, Variant.INDEX)
        assertEquals(0, result.size)

        region = Region(Point(x = 5.0, y = 1.0), Point(x = 5.0, y = 1.0), tree = tree)
        result = tree.leavesSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("2", result[0])

        region = Region(Point(x = 2.0, y = 1.0), Point(x = 2.0, y = 1.0), tree = tree)
        result = tree.leavesSearch(region, Variant.INDEX)
        assertEquals(1, result.size)
        assertEquals("3", result[0])
    }
}