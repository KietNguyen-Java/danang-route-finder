package com.kietnguyen.routefinder.algo;

import java.util.List;

/**
 * A path as a list of nodes, its total cost in the searched metric, and how many nodes the
 * search had to settle to find it (the number that shows A* doing less work than Dijkstra).
 */
public record PathResult(List<Integer> nodes, double cost, int nodesExpanded) {

    public static PathResult notFound(int nodesExpanded) {
        return new PathResult(List.of(), Double.POSITIVE_INFINITY, nodesExpanded);
    }

    public boolean found() {
        return !nodes.isEmpty();
    }
}
