import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

internal class TrajectoryTest {

    private val dataNodes = arrayListOf(
        DataNode(0.0, 3.0, "1"),
        DataNode(1.0, 5.0, "1"),
        DataNode(2.0, 6.0, "1"),
        DataNode(8.0, 3.0, "1"),
        DataNode(9.0, 5.0, "1"),
        DataNode(10.0, 6.0, "1"),
    )


    private val splitNodes = arrayListOf(
        DataNode(1.0, 1.0, "2"),
        DataNode(2.0, 2.0, "2"),
        DataNode(3.0, 3.0, "2"),
        DataNode(4.0, 4.0, "2"),
        DataNode(8.0, 8.0, "2"),
        DataNode(9.0, 9.0, "2"),
        DataNode(10.0, 10.0, "2"),
        DataNode(11.0, 11.0, "2"),
    )

    private val trajectory1 = Trajectory(dataNodes, "1")
    private val trajectory2 = Trajectory(splitNodes, "2")

    @BeforeTest
    fun initializeTrajectories() {
        trajectory1.initialize()
        trajectory2.initialize()
    }

    @Test
    fun testLastNodeOnlyContainsItsOwnMBR() {
        assertEquals(
            BoundingRectangle(Point(10.0, 6.0), Point(10.0, 6.0)), trajectory1.getNodes().last().getBoundingRectangle()
        )
    }

    @Test
    fun testFirstNodeContainsAllMBRs() {
        assertEquals(
            BoundingRectangle(Point(0.0, 6.0), Point(10.0, 3.0)), trajectory1.getNodes().first().getBoundingRectangle()
        )
    }

    @Test
    fun testLastNodeHasRightParent() {
        assertEquals(dataNodes[1].getId(), trajectory1.getNodes()[2].getParentId())
    }

    @Test
    fun testFirstNodeHasNoParent() {
        assertEquals(null, trajectory1.getNodes().first().getParentId())
    }

    @Test
    fun testSplitting() {
        println(trajectory2.getSplits()[0].first().getBoundingRectangleArea())
        println(trajectory2.getSplits()[1].first().getBoundingRectangleArea())
        trajectory2.getSplits().forEach { it.forEach { node -> println(node.getId()) } }
        assertEquals(8, trajectory2.getSplits().sumOf { it.size })
        assertEquals(4, trajectory2.getSplits()[1].size)
        assertEquals(4, trajectory2.getSplits()[0].size)
    }

    @Test
    fun testFindingRectangle() {
        trajectory2.getNodes().forEach { println(it) }
        assertEquals(Pair(trajectory2.getNodes()[3], trajectory2.getNodes()[4]), trajectory2.findSmallestRectangles())
    }

    @Test
    fun recalculateMBRTest() {
        trajectory1.recalculateMBR(trajectory1.getNodes())
        assertEquals(30.0, trajectory1.getNodes().first().getBoundingRectangleArea())
    }
}