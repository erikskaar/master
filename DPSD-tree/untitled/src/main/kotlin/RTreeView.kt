import javafx.scene.paint.Color
import rTree.RTreeViewController
import rTree.Tree
import tornadofx.*

class RTreeView : View() {
    private val MULTIPLIER = 5000
    private val controller: RTreeViewController by inject()
    private val tree: Tree = controller.values[0]

    override val root = vbox {
        stackpane {
            group {
                tree.getAllPoints().forEachIndexed { i, it ->
                        val color = c(Math.random(), Math.random(), Math.random())
                        label {
                            textFill = color.apply { setOpacity(0.0) }
                            layoutX = it.coordinates.x * MULTIPLIER
                            layoutY = it.coordinates.y * MULTIPLIER
                            text = i.toString()
                        }
                        circle {
                            fill = c(color.toString())
                            opacity = 1.0
                            radius = 2.0
                            centerX = it.coordinates.x * MULTIPLIER
                            centerY = it.coordinates.y * MULTIPLIER
                        }
                }
                tree.getAllRegions().forEach {
                    val color: Color = if (it.getIsLeaf()) {
                        c(1.0, 0.0, 0.0)
                    } else if(it.getIsRoot()){
                        c(0.0, 0.0, 0.0)
                    }
                    else {
                        c(0.0, 0.0, 1.0)
                    }
                    polyline(points = Utils.getRectanglePointsForRegion(it, MULTIPLIER)) {
                        stroke = c(color.toString(), opacity = 1.0)
                        //strokeWidth = (5-it.getDirectSubRegions().size).toDouble()
                    }
                }
            }
        }
    }
}

class RTreeViewApp : App(RTreeView::class, Styles::class)
