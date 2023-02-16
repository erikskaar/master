package rTree

import javafx.beans.value.ObservableObjectValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller


class RTreeViewController : Controller() {
    private val nodes = CSVReader.readCSV("converted_shortened.csv", "rTree")

    val values: ObservableList<Tree> = FXCollections.observableArrayList(Tree(nodes as ArrayList<PointNode>))
}