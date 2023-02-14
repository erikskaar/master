data class Point(val x: Double, val y: Double) {
    fun isDominatedByNorthWest(point: Point): Boolean {
        return x > point.x && y < point.y
    }
}
