import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

object CSVReader {
    fun readCSV(file: String): ArrayList<DataNode> {
        val f = File(file)
        val dataNodes = arrayListOf<DataNode>()
        var counter = 0
        var lineCounter = 0
        csvReader().open(f) {
            println("Reading file")
            val fileSizePercent = f.readLines().size / 99
            run loop@{
                readAllWithHeaderAsSequence().forEachIndexed { index, it ->
                    lineCounter++
                    if (index % fileSizePercent == 0) {
                        counter++
                        println("$counter%")
                    }
                    dataNodes.add(
                        DataNode(
                            x = it.get("LATITUDE")!!.toDouble(),
                            y = it.get("LONGITUDE")!!.toDouble(),
                            trajectoryId = it.get("TRIP_ID")!!
                        )
                    )
                    if (lineCounter > 5000) return@loop
                }
            }
        }
        println("Nodes created")
        return dataNodes
    }

    fun addNodesToTrajectories(dataNodes: ArrayList<DataNode>): ArrayList<Trajectory> {

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