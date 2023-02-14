import javafx.collections.ObservableList
import tornadofx.*

class ChartView : View() {
    private val MULTIPLIER = 5000
    private val controller: ViewController by inject()
    private val trajectories: ObservableList<Trajectory> = controller.values

    private fun getRectanglePoints(rectangle: BoundingRectangle): Array<Number> {
        return arrayOf(
            rectangle.northWest.x * MULTIPLIER,
            rectangle.northWest.y * MULTIPLIER,

            rectangle.southEast.x * MULTIPLIER,
            rectangle.northWest.y * MULTIPLIER,

            rectangle.southEast.x * MULTIPLIER,
            rectangle.southEast.y * MULTIPLIER,

            rectangle.northWest.x * MULTIPLIER,
            rectangle.southEast.y * MULTIPLIER,

            rectangle.northWest.x * MULTIPLIER,
            rectangle.northWest.y * MULTIPLIER,
        )
    }

    override val root = vbox {
        stackpane {
            group {
                trajectories.forEachIndexed { i, trajectory ->
                    if (i > -1) {
                        val color = c(Math.random(), Math.random(), Math.random())
                        polyline(points = getRectanglePoints(trajectory.getBoundingRectangle())) {
                            stroke = c(color.toString(), opacity = 0.2)
                        }
                        trajectory.getSplits().forEach { split ->
                            split.forEachIndexed { index, it ->
                                label {
                                    textFill = color.apply { setOpacity(0.0) }
                                    layoutX = it.getX() * MULTIPLIER
                                    layoutY = it.getY() * MULTIPLIER
                                    text = index.toString()
                                }
                                circle {
                                    fill = c(color.toString())
                                    radius = 2.0
                                    centerX = it.getX() * MULTIPLIER
                                    centerY = it.getY() * MULTIPLIER
                                }

                                polyline(points = getRectanglePoints(it.getBoundingRectangle())) {
                                    stroke = c(color.toString(), opacity = 1.0)
                                }
                            }
                        }
                    }
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
    }
}

class ChartViewApp : App(ChartView::class, Styles::class)
