package com.kietnguyen.routefinder.algo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KShortestPathsTest {

    @Test
    void solvesTheTextbookExample() {
        // The example graph from the Wikipedia article on Yen's algorithm: C D E F G H = 0..5.
        Graph g = new Graph(6);
        int c = 0, d = 1, e = 2, f = 3, gNode = 4, h = 5;
        g.addEdge(c, d, 3, 50);
        g.addEdge(c, e, 2, 50);
        g.addEdge(d, f, 4, 50);
        g.addEdge(e, d, 1, 50);
        g.addEdge(e, f, 2, 50);
        g.addEdge(e, gNode, 3, 50);
        g.addEdge(f, gNode, 2, 50);
        g.addEdge(f, h, 1, 50);
        g.addEdge(gNode, h, 2, 50);

        List<PathResult> paths = KShortestPaths.find(g, c, h, 3, Metric.DISTANCE);
        assertEquals(List.of(c, e, f, h), paths.get(0).nodes());
        assertEquals(5, paths.get(0).cost());
        assertEquals(List.of(c, e, gNode, h), paths.get(1).nodes());
        assertEquals(7, paths.get(1).cost());
        // Two paths tie for third place at cost 8: C-D-F-H and C-E-F-G-H. Either is correct.
        assertEquals(8, paths.get(2).cost());
        assertTrue(List.of(List.of(c, d, f, h), List.of(c, e, f, gNode, h)).contains(paths.get(2).nodes()));
    }

    @Test
    void matchesBruteForceOnSmallRandomGraphs() {
        Random random = new Random(11);
        for (int trial = 0; trial < 60; trial++) {
            Graph g = ShortestPathsTest.randomGraph(random, 7, 20);
            int source = random.nextInt(7);
            int target = random.nextInt(7);
            if (source == target) {
                continue;
            }
            List<Double> expected = allSimplePathCosts(g, source, target);
            int k = 4;
            List<PathResult> found = KShortestPaths.find(g, source, target, k, Metric.DISTANCE);

            assertEquals(Math.min(k, expected.size()), found.size(), "trial " + trial);
            HashSet<List<Integer>> distinct = new HashSet<>();
            for (int i = 0; i < found.size(); i++) {
                List<Integer> nodes = found.get(i).nodes();
                assertEquals(nodes.size(), new HashSet<>(nodes).size(), "paths must be loopless");
                assertTrue(distinct.add(nodes), "paths must be distinct");
                assertEquals(expected.get(i), found.get(i).cost(), 1e-6, "trial " + trial + " path " + i);
            }
        }
    }

    /** Every loopless path by depth-first search, cheapest first. */
    private static List<Double> allSimplePathCosts(Graph g, int source, int target) {
        List<Double> costs = new ArrayList<>();
        dfs(g, source, target, new boolean[g.size()], 0, costs);
        costs.sort(Comparator.naturalOrder());
        return costs;
    }

    private static void dfs(Graph g, int u, int target, boolean[] onPath, double cost, List<Double> out) {
        if (u == target) {
            out.add(cost);
            return;
        }
        onPath[u] = true;
        // Parallel roads between the same two places count as one path, using the cheapest road.
        double[] best = new double[g.size()];
        java.util.Arrays.fill(best, Double.POSITIVE_INFINITY);
        for (Graph.Edge e : g.edgesFrom(u)) {
            best[e.to()] = Math.min(best[e.to()], e.meters());
        }
        for (int v = 0; v < g.size(); v++) {
            if (!onPath[v] && Double.isFinite(best[v])) {
                dfs(g, v, target, onPath, cost + best[v], out);
            }
        }
        onPath[u] = false;
    }
}
