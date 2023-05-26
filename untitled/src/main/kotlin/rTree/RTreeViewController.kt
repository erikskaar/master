package rTree

import CSVReader
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller

class RTreeViewController : Controller() {
    private val nodes = CSVReader.readCSV("converted_shortened.csv", "rTree", 10_000)

    val values: ObservableList<Tree> = FXCollections.observableArrayList(Tree(nodes as ArrayList<PointNode>))
}