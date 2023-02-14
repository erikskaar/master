class Trajectory(private val nodes: ArrayList<DataNode>, private val id: String) {
    private var isSplit: Boolean = false
    private val splits: ArrayList<ArrayList<DataNode>> = arrayListOf(nodes)
    private var boundingRectangle: BoundingRectangle = nodes.first().getBoundingRectangle()

    fun getBoundingRectangle() = boundingRectangle

    private fun setParentsAndChildren() {
        var previousNode: Node? = null
        splits.forEach {
            it.forEach { node ->
                node.setParent(previousNode)
                previousNode?.setChild(node)
                previousNode = node
            }
            previousNode = null
        }
    }

    private fun setBoundingRectangles() {
        splits.forEach {
            it.asReversed().forEach { node ->
                node.setParentMBR()
            }
        }
    }


    fun recalculateMBR(list: ArrayList<DataNode>) {
        if (list.isEmpty()) return
        var previousNode: DataNode? = null
        list.forEach { node ->
            node.setParent(previousNode)
            previousNode = node

        }
        list.forEach { it.resetBoundingRectangle() }
        list.asReversed().forEach { node ->
            node.setParentMBR()
        }
    }

    fun findSmallestRectangles(): Pair<Node, Node?> {
        val area1 = nodes
        val area2 = arrayListOf<DataNode>()
        var smallestArea = Double.MAX_VALUE

        area1.forEach { it.resetBoundingRectangle() }
        area2.add(area1.removeFirst())
        var split: Pair<Node, Node?> = Pair(area1.first(), area2.first())

        while (area1.size > 0) {
            area2.add(area1.removeFirst())
            recalculateMBR(area1)
            recalculateMBR(area2)
            val totalArea = if (area1.size > 1 && area2.size > 1) {
                area1.first().getBoundingRectangleArea() + area2.first().getBoundingRectangleArea()
            } else Double.MAX_VALUE

            if (totalArea < smallestArea) {
                smallestArea = totalArea
                split = Pair(area2.last(), area1.first())
            }
        }
        return split
    }

    private fun split() {
        if (nodes.size < 8) return
        val splitPair = findSmallestRectangles()
        if (splitPair.second === null) return
        splits.clear()
        var currentNode = splitPair.second!!
        val firstSplit = arrayListOf<Node>()
        while (currentNode.getParent() !== null) {
            firstSplit.add(currentNode.getParent()!!)
            currentNode = currentNode.getParent()!!
        }
        firstSplit.forEach { it.resetBoundingRectangle() }
        currentNode = splitPair.first
        val secondSplit = arrayListOf<Node>()
        while (currentNode.getChild() !== null) {
            secondSplit.add(currentNode.getChild()!!)
            currentNode = currentNode.getChild()!!
        }
        secondSplit.forEach { it.resetBoundingRectangle() }
        firstSplit.reverse()
        splits.add(firstSplit as ArrayList<DataNode>)
        splits.add(secondSplit as ArrayList<DataNode>)
        setParentsAndChildren()
        setBoundingRectangles()
        boundingRectangle = BoundingRectangle(
            northWest = Point(
                splits.map { split -> split.map { node -> node.getBoundingRectangle().northWest.x }.minOf { it } }
                    .minOf { it },
                splits.map { split -> split.map { node -> node.getBoundingRectangle().northWest.y }.maxOf { it } }
                    .maxOf { it },
            ), southEast = Point(
                splits.map { split -> split.map { node -> node.getBoundingRectangle().southEast.x }.maxOf { it } }
                    .maxOf { it },
                splits.map { split -> split.map { node -> node.getBoundingRectangle().southEast.y }.minOf { it } }
                    .minOf { it },
            )
        )
        isSplit = true
    }

    fun getNodes() = nodes

    fun getId() = id

    fun addNode(dataNode: DataNode) {
        nodes.add(dataNode)
    }

    fun getNumberOfNodes() = nodes.size

    fun getIsSplit() = isSplit

    fun getSplits() = splits

    fun initialize() {
        setParentsAndChildren()
        setBoundingRectangles()
        split()
    }
}