import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import custom.DataNode
import custom.Trajectory
import rTree.PointNode
import java.io.File

object CSVReader {
    fun readCSV(file: String, type: String): Any {
        val f = File(file)
        val dataNodes = arrayListOf<DataNode>()
        val pointNodes = arrayListOf<PointNode>()
        var counter = 0
        var lineCounter = 0
        val MAX_LINES = 500
        csvReader().open(f) {
            println("Reading file")
            val fileSizePercent = f.readLines().size / 99
            run loop@{
                readAllWithHeaderAsSequence().forEachIndexed { index, it ->
                    lineCounter++
                    if (lineCounter > MAX_LINES) return@loop
                    if (index % fileSizePercent == 0) {
                        counter++
                        println("$counter% of CSV parsed")
                    }
                    when (type) {
                        "custom" -> {
                            dataNodes.add(
                                DataNode(
                                    x = it["LATITUDE"]!!.toDouble(),
                                    y = it["LONGITUDE"]!!.toDouble(),
                                    trajectoryId = it["TRIP_ID"]!!
                                )
                            )
                        }
                        "rTree" -> {
                            pointNodes.add(
                                PointNode(
                                    coordinates = Point(
                                        x = it["LATITUDE"]!!.toDouble(),
                                        y = it["LONGITUDE"]!!.toDouble()
                                    ),
                                    trajectoryId = it["TRIP_ID"]!!
                                )
                            )
                        }
                        else -> return@open
                    }

                }
            }
        }
        return when (type) {
            "custom" -> addDataNodesToTrajectories(dataNodes)
            "rTree" -> pointNodes
            else -> arrayListOf("")
        }
    }

    private fun addDataNodesToTrajectories(dataNodes: ArrayList<DataNode>): ArrayList<Trajectory> {
        val trajectories = arrayListOf<Trajectory>()
        var previousTrajectoryId = ""
        var previousTrajectory: Trajectory? = null

        dataNodes.forEach {
            if (it.getTrajectoryId() == previousTrajectoryId) {
                previousTrajectory?.addNode(it)
            } else {
                previousTrajectory = Trajectory(arrayListOf(it), it.getTrajectoryId())
            }
            if (previousTrajectoryId !== previousTrajectory?.getId()) {
                trajectories.add(previousTrajectory!!)
                previousTrajectoryId = previousTrajectory!!.getId()
            }
        }
        println("Nodes added to trajectories")

        trajectories.forEach {
            it.initialize()
        }
        println("${trajectories.size} trajectories initialized")
        return trajectories
    }
}