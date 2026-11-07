package com.kietnguyen.routefinder.algo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.IntToDoubleFunction;

/**
 * Single-pair shortest path algorithms on a {@link Graph}.
 *
 * <ul>
 *   <li>Dijkstra: O((V + E) log V) with a binary heap. Correct for non-negative weights.</li>
 *   <li>A*: Dijkstra ordered by {@code cost so far + lower bound to target}. With a consistent
 *       lower bound it returns the same cost as Dijkstra but usually settles far fewer nodes.</li>
 *   <li>BFS: fewest roads, ignoring length. O(V + E).</li>
 * </ul>
 */
public final class ShortestPaths {

    /** Nodes and edges a search may not use. Yen's algorithm needs this. */
    record Restrictions(boolean[] bannedNodes, Set<Long> bannedEdges) {
        static final Restrictions NONE = new Restrictions(null, Set.of());

        boolean nodeBanned(int node) {
            return bannedNodes != null && bannedNodes[node];
        }

        boolean edgeBanned(Graph graph, Graph.Edge edge) {
            return !bannedEdges.isEmpty() && bannedEdges.contains(edgeKey(graph, edge.from(), edge.to()));
        }
    }

    private record QueueEntry(int node, double priority) {
    }

    private ShortestPaths() {
    }

    static long edgeKey(Graph graph, int from, int to) {
        return (long) from * graph.size() + to;
    }

    public static PathResult dijkstra(Graph graph, int source, int target, Metric metric) {
        return search(graph, source, target, metric, node -> 0, Restrictions.NONE);
    }

    public static PathResult aStar(Graph graph, int source, int target, Metric metric) {
        return search(graph, source, target, metric, node -> metric.lowerBound(graph, node, target), Restrictions.NONE);
    }

    /**
     * Best-first search. With {@code heuristic == 0} this is Dijkstra; otherwise A*.
     * Uses lazy deletion: a node can sit in the heap several times, and stale entries are skipped
     * when popped, which is simpler and usually faster than a decrease-key heap.
     */
    static PathResult search(Graph graph, int source, int target, Metric metric,
                             IntToDoubleFunction heuristic, Restrictions restrictions) {
        int n = graph.size();
        double[] dist = new double[n];
        int[] previous = new int[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(previous, -1);

        if (restrictions.nodeBanned(source)) {
            return PathResult.notFound(0);
        }
        PriorityQueue<QueueEntry> heap = new PriorityQueue<>((a, b) -> Double.compare(a.priority(), b.priority()));
        dist[source] = 0;
        heap.add(new QueueEntry(source, heuristic.applyAsDouble(source)));
        int expanded = 0;

        while (!heap.isEmpty()) {
            int u = heap.poll().node();
            if (settled[u]) {
                continue; // stale entry
            }
            settled[u] = true;
            expanded++;
            if (u == target) {
                return new PathResult(walkBack(previous, target), dist[target], expanded);
            }
            for (Graph.Edge e : graph.edgesFrom(u)) {
                int v = e.to();
                if (settled[v] || restrictions.nodeBanned(v) || restrictions.edgeBanned(graph, e)) {
                    continue;
                }
                double candidate = dist[u] + metric.weight(e);
                if (candidate < dist[v]) {
                    dist[v] = candidate;
                    previous[v] = u;
                    heap.add(new QueueEntry(v, candidate + heuristic.applyAsDouble(v)));
                }
            }
        }
        return PathResult.notFound(expanded);
    }

    /** Shortest distance from {@code source} to every node (Dijkstra without a target). */
    public static double[] distancesFrom(Graph graph, int source, Metric metric) {
        int n = graph.size();
        double[] dist = new double[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        PriorityQueue<QueueEntry> heap = new PriorityQueue<>((a, b) -> Double.compare(a.priority(), b.priority()));
        dist[source] = 0;
        heap.add(new QueueEntry(source, 0));
        while (!heap.isEmpty()) {
            int u = heap.poll().node();
            if (settled[u]) {
                continue;
            }
            settled[u] = true;
            for (Graph.Edge e : graph.edgesFrom(u)) {
                double candidate = dist[u] + metric.weight(e);
                if (candidate < dist[e.to()]) {
                    dist[e.to()] = candidate;
                    heap.add(new QueueEntry(e.to(), candidate));
                }
            }
        }
        return dist;
    }

    /** Path with the fewest roads. {@code cost} is the number of roads. */
    public static PathResult fewestRoads(Graph graph, int source, int target) {
        int n = graph.size();
        int[] previous = new int[n];
        boolean[] seen = new boolean[n];
        Arrays.fill(previous, -1);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        seen[source] = true;
        int expanded = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            expanded++;
            if (u == target) {
                List<Integer> path = walkBack(previous, target);
                return new PathResult(path, path.size() - 1, expanded);
            }
            for (Graph.Edge e : graph.edgesFrom(u)) {
                if (!seen[e.to()]) {
                    seen[e.to()] = true;
                    previous[e.to()] = u;
                    queue.add(e.to());
                }
            }
        }
        return PathResult.notFound(expanded);
    }

    /** Cost of following {@code path}, using the cheapest road between each pair of nodes. */
    public static double pathCost(Graph graph, List<Integer> path, Metric metric) {
        double total = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            total += cheapestEdge(graph, path.get(i), path.get(i + 1), metric).map(metric::weight)
                    .orElseThrow(() -> new IllegalArgumentException("no road along the path"));
        }
        return total;
    }

    public static Optional<Graph.Edge> cheapestEdge(Graph graph, int from, int to, Metric metric) {
        Graph.Edge best = null;
        for (Graph.Edge e : graph.edgesFrom(from)) {
            if (e.to() == to && (best == null || metric.weight(e) < metric.weight(best))) {
                best = e;
            }
        }
        return Optional.ofNullable(best);
    }

    private static List<Integer> walkBack(int[] previous, int target) {
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = previous[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }
}
