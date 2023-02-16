package custom

import CSVReader
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller

class CustomViewController: Controller() {

    private val nodes = CSVReader.readCSV("converted_shortened.csv", "custom")
    private val trajectories = nodes as ArrayList<Trajectory>

    val values: ObservableList<Trajectory> = FXCollections.observableArrayList(trajectories)

}