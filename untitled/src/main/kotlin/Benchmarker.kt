import rTree.PointNode
import rTree.Tree
import java.io.File

object Benchmarker {
    private var points: ArrayList<PointNode> = arrayListOf()
    var output: Output = Output.CONSOLE
    private lateinit var tree: Tree

    fun loadData(numberOfPoints: Int): Tree {
        // println("Loading data...")
        val points = CSVReader.readCSV("converted_shortened.csv", "rTree", numberOfPoints)
        tree = Tree(points as ArrayList<PointNode>)
        // println("Loading completed.")
        return tree
    }

    fun updatePoints(maxPoints: Int) {
        if (points.size < maxPoints) return
        val newPoints = points.subList(0, maxPoints) as ArrayList<PointNode>
        Utils.createPointNodeLinks(newPoints)
        tree = Tree(newPoints)
    }

    fun benchmarkQuery(type: Search, times: Int, fileName: String, numberOfPoints: Int) {
        tree.speedCheck(type, times, output, fileName, numberOfPoints)
    }

    fun benchmarkTreeCreation(numberOfPoints: Int) {
        val file = File("benchmark_tree_creation.csv")
        file.writeText("Points, Time, Depth, Regions, Leaf Nodes\n")
        for (x in 10_000..numberOfPoints step 1_000) {
            val points = CSVReader.readCSV("converted_shortened.csv", "rTree", x)
            val tree = Tree(points as ArrayList<PointNode>)
            file.appendText("$x, now, ${tree.getDepth()}, ${tree.getAllRegions().count()}, ${tree.getLeafNodes().count()}\n")
            if (x%1000==0) println(x)
        }
        println("Tree creation finished")
    }

    fun benchmarkRangeSearch(numberOfPoints: Int) {
        writeHeadersToFile("benchmark_range_search.csv")
        for (x in 10_000..numberOfPoints step 10_000) {
            val points = CSVReader.readCSV("converted_shortened.csv", "rTree", x)
            val tree = Tree(points as ArrayList<PointNode>)
            tree.speedCheck(Search.RANGE_SEARCH, 10, output, "benchmark_range_search.csv", x)
        }
    }

    fun writeHeadersToFile(fileName: String) {
        val file = File(fileName)
        file.writeText("Timestamp, Name, Average, Median, Max, Min, Speeds, Points\n")
    }
}