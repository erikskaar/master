enum class Search(val outputName: String) {
    TRAJECTORY_START_AND_END_REGION_SEARCH("Iterative Start and End Search"),
    TRAJECTORY_START_AND_END_REGION_SEARCH_WITH_LINKS("Linked Start and End Search"),
    TRAJECTORY_START_AND_END_REGION_SEARCH_WITH_INDEX("Indexed Start and End Search"),
    TRAJECTORY_PASSES_THROUGH_REGION_SEARCH("Iterative Passes Through Search"),
    TRAJECTORY_PASSES_THROUGH_REGION_SEARCH_WITH_LINKS("Linked Passes Through Search"),
    TRAJECTORY_PASSES_THROUGH_REGION_SEARCH_WITH_INDEX("Indexed Passes Through Search"),
    TRAJECTORY_ENTERS_REGION_SEARCH("Iterative Enters Search"),
    TRAJECTORY_ENTERS_REGION_SEARCH_WITH_LINKS("Linked Enters Search"),
    TRAJECTORY_ENTERS_REGION_SEARCH_WITH_INDEX("Indexed Enters Search"),
    TRAJECTORY_LEAVES_REGION_SEARCH("Iterative Leaves Search"),
    TRAJECTORY_LEAVES_REGION_SEARCH_WITH_LINKS("Linked Leaves Search"),
    TRAJECTORY_LEAVES_REGION_SEARCH_WITH_INDEX("Indexed Leaves Search"),
    RANGE_SEARCH("Range Search")
}

enum class Variant {
    LINKED_LIST,
    INDEX,
    ITERATIVE
}