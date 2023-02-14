import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller

class ViewController: Controller() {

    private val nodes = CSVReader.readCSV("converted_shortened.csv")
    private val trajectories = CSVReader.addNodesToTrajectories(nodes)

    val values: ObservableList<Trajectory> = FXCollections.observableArrayList(trajectories)

}