import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import rTree.Point
import rTree.PointNode
import java.io.File

object CSVReader {
    /**
     * Reads a CSV file and returns an ArrayList of PointNodes
     */
    fun readCSV(file: String, type: String, maxLines: Int): Any {
        val f = File(file)
        val pointNodes = arrayListOf<PointNode>()
        var counter = 0
        var lineCounter = 0
        // println("Reading file...")
        csvReader().open(f) {
            run loop@{
                readAllWithHeaderAsSequence().forEach{
                    lineCounter++
                    if (lineCounter > maxLines) return@loop
                    when (type) {
                        "rTree" -> {
                            pointNodes.add(
                                PointNode(
                                    coordinates = Point(
                                        x = (it["LATITUDE"]!!.toDouble() + 9),
                                        y = (-(it["LONGITUDE"]!!).toDouble() + 42)
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
            "rTree" -> Utils.createPointNodeLinks(pointNodes)
            else -> arrayListOf("")
        }
    }


}