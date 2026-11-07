package com.kietnguyen.routefinder.algo;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortestPathsTest {

    /** Random places in a 10 km square near Da Nang; every road is 1.0-1.5x the straight line. */
    static Graph randomGraph(Random random, int nodes, int edges) {
        Graph g = new Graph(nodes);
        for (int i = 0; i < nodes; i++) {
            g.setLocation(i, 16.0 + random.nextDouble() * 0.09, 108.15 + random.nextDouble() * 0.09);
        }
        for (int k = 0; k < edges; k++) {
            int a = random.nextInt(nodes);
            int b = random.nextInt(nodes);
            if (a != b) {
                double meters = g.straightLineMeters(a, b) * (1 + random.nextDouble() * 0.5) + 1;
                g.addEdge(a, b, meters, 20 + random.nextInt(60));
            }
        }
        return g;
    }

    /** Reference answer: Bellman-Ford, O(V·E), simple enough to trust. */
    static double[] bellmanFord(Graph g, int source, Metric metric) {
        double[] dist = new double[g.size()];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        dist[source] = 0;
        for (int round = 0; round < g.size() - 1; round++) {
            for (int u = 0; u < g.size(); u++) {
                for (Graph.Edge e : g.edgesFrom(u)) {
                    dist[e.to()] = Math.min(dist[e.to()], dist[u] + metric.weight(e));
                }
            }
        }
        return dist;
    }

    @Test
    void dijkstraAndAStarMatchBellmanFordOnRandomGraphs() {
        Random random = new Random(2026);
        long dijkstraWork = 0;
        long aStarWork = 0;
        for (int trial = 0; trial < 40; trial++) {
            Graph g = randomGraph(random, 60, 240);
            for (Metric metric : Metric.values()) {
                int source = random.nextInt(g.size());
                double[] expected = bellmanFord(g, source, metric);
                for (int target = 0; target < g.size(); target++) {
                    PathResult d = ShortestPaths.dijkstra(g, source, target, metric);
                    PathResult a = ShortestPaths.aStar(g, source, target, metric);
                    if (Double.isInfinite(expected[target])) {
                        assertFalse(d.found());
                        assertFalse(a.found());
                        continue;
                    }
                    assertEquals(expected[target], d.cost(), 1e-6);
                    assertEquals(expected[target], a.cost(), 1e-6, "A* must be exact with an admissible heuristic");
                    assertEquals(d.cost(), ShortestPaths.pathCost(g, d.nodes(), metric), 1e-6);
                    assertEquals(a.cost(), ShortestPaths.pathCost(g, a.nodes(), metric), 1e-6);
                    dijkstraWork += d.nodesExpanded();
                    aStarWork += a.nodesExpanded();
                }
                assertTrue(Arrays.equals(expected, ShortestPaths.distancesFrom(g, source, metric))
                        || closeEnough(expected, ShortestPaths.distancesFrom(g, source, metric)));
            }
        }
        assertTrue(aStarWork < dijkstraWork, "A* should settle fewer nodes: " + aStarWork + " vs " + dijkstraWork);
    }

    private static boolean closeEnough(double[] a, double[] b) {
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) > 1e-6 && !(Double.isInfinite(a[i]) && Double.isInfinite(b[i]))) {
                return false;
            }
        }
        return true;
    }

    @Test
    void timeMetricPrefersFastRoads() {
        // 0 -> 1 -> 3 is short but slow; 0 -> 2 -> 3 is longer but on a fast road.
        Graph g = new Graph(4);
        g.addEdge(0, 1, 1_000, 10);
        g.addEdge(1, 3, 1_000, 10);
        g.addEdge(0, 2, 1_500, 80);
        g.addEdge(2, 3, 1_500, 80);
        assertEquals(List.of(0, 1, 3), ShortestPaths.dijkstra(g, 0, 3, Metric.DISTANCE).nodes());
        assertEquals(List.of(0, 2, 3), ShortestPaths.dijkstra(g, 0, 3, Metric.TIME).nodes());
    }

    @Test
    void bfsFindsFewestRoads() {
        Graph g = new Graph(5);
        g.addEdge(0, 1, 10, 50);
        g.addEdge(1, 2, 10, 50);
        g.addEdge(2, 4, 10, 50);
        g.addEdge(0, 3, 500, 50);
        g.addEdge(3, 4, 500, 50);
        assertEquals(List.of(0, 3, 4), ShortestPaths.fewestRoads(g, 0, 4).nodes());
        assertEquals(List.of(0, 1, 2, 4), ShortestPaths.dijkstra(g, 0, 4, Metric.DISTANCE).nodes());
    }

    @Test
    void respectsOneWayRoads() {
        Graph g = new Graph(2);
        g.addEdge(0, 1, 100, 30);
        assertTrue(ShortestPaths.dijkstra(g, 0, 1, Metric.DISTANCE).found());
        assertFalse(ShortestPaths.dijkstra(g, 1, 0, Metric.DISTANCE).found());
    }

    @Test
    void pathToSelfIsJustTheStart() {
        Graph g = new Graph(1);
        PathResult p = ShortestPaths.aStar(g, 0, 0, Metric.DISTANCE);
        assertEquals(List.of(0), p.nodes());
        assertEquals(0, p.cost());
    }
}
