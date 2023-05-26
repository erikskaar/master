import rTree.Point
import rTree.RTreeViewController
import rTree.Region
import rTree.Tree
import tornadofx.*

class RTreeView : View() {
    private val MULTIPLIER = 5_000
    private val controller: RTreeViewController by inject()
    private val tree: Tree = controller.values[0]
    private val startRegion = Region(Point(0.385, 0.865), Point(0.395, 0.850), tree = tree)
    private val endRegion = Region(Point(0.370, 0.845), Point(0.380, 0.830), tree = tree)
    private val trajectories = tree.entersSearch(startRegion, Variant.LINKED_LIST)

    override val root = vbox {
        stackpane {
            group {
                var color = c(0.0, 0.0, 0.0)
                tree.getAllPoints().forEachIndexed { i, it ->
                    label {
                        textFill = color.apply { setOpacity(0.0) }
                        layoutX = it.coordinates.x * MULTIPLIER
                        layoutY = it.coordinates.y * MULTIPLIER
                        text = i.toString()
                    }
                    circle {
                        fill = Utils.hashToColor(it.trajectoryId)
                        opacity = if (trajectories.contains(it.trajectoryId)) 1.0 else 0.0
                        radius = 1.0
                        centerX = it.coordinates.x * MULTIPLIER
                        centerY = it.coordinates.y * MULTIPLIER
                    }
                }
                tree.getAllRegions().forEach {
                    color = if (it.getIsLeaf()) {
                        c(1.0, 0.0, 0.0)
                    } else if (it.getIsRoot()) {
                        c(0.0, 0.0, 0.0)
                    } else {
                        c(0.0, 0.0, 1.0)
                    }
                    polyline(points = Utils.getRectanglePointsForRegion(it, MULTIPLIER)) {
                        stroke = c(color.toString(), opacity = 0.0)
                    }
                }
                polyline(points = Utils.getRectanglePointsForRegion(startRegion, MULTIPLIER)) {
                    stroke = c(1.0, 0.0, 0.0, opacity = 1.0)
                    strokeWidth = 4.0
                }
                polyline(points = Utils.getRectanglePointsForRegion(endRegion, MULTIPLIER)) {
                    stroke = c(0.0, 0.0, 1.0, opacity = 1.0)
                    strokeWidth = 4.0
                }
            }
        }
    }
}

class Styles : Stylesheet() {
    init {
        Companion.label {
            fontFamily = "serif"
        }
        Companion.root {
            padding = box(20.px)
            fontFamily = "serif"
            backgroundColor = multi(c("#ffffff"))
        }
    }
}

class RTreeViewApp : App(RTreeView::class, Styles::class)
