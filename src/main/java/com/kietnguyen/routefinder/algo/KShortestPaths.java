package com.kietnguyen.routefinder.algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Yen's algorithm: the k shortest loopless paths between two nodes, cheapest first.
 *
 * <p>Path {@code i+1} is found by taking each prefix ("root") of path {@code i}, forbidding the
 * roads that earlier paths with the same root used next, forbidding the root's nodes so the new
 * path stays loopless, and searching for the cheapest "spur" from the root's last node to the
 * target. The cheapest root + spur not seen before becomes the next path.
 *
 * <p>Cost: O(k · V · (V + E) log V) Dijkstra work in the worst case.
 */
public final class KShortestPaths {

    private KShortestPaths() {
    }

    public static List<PathResult> find(Graph graph, int source, int target, int k, Metric metric) {
        List<PathResult> accepted = new ArrayList<>();
        PathResult first = ShortestPaths.dijkstra(graph, source, target, metric);
        if (!first.found() || k <= 0) {
            return accepted;
        }
        accepted.add(first);

        PriorityQueue<PathResult> candidates = new PriorityQueue<>((a, b) -> Double.compare(a.cost(), b.cost()));
        Set<List<Integer>> known = new HashSet<>();
        known.add(first.nodes());

        while (accepted.size() < k) {
            List<Integer> last = accepted.getLast().nodes();
            for (int i = 0; i + 1 < last.size(); i++) {
                int spurNode = last.get(i);
                List<Integer> root = last.subList(0, i + 1);

                Set<Long> bannedEdges = new HashSet<>();
                for (PathResult p : accepted) {
                    List<Integer> nodes = p.nodes();
                    if (nodes.size() > i + 1 && nodes.subList(0, i + 1).equals(root)) {
                        bannedEdges.add(ShortestPaths.edgeKey(graph, nodes.get(i), nodes.get(i + 1)));
                    }
                }
                boolean[] bannedNodes = new boolean[graph.size()];
                for (int r = 0; r < i; r++) {
                    bannedNodes[root.get(r)] = true;
                }

                PathResult spur = ShortestPaths.search(graph, spurNode, target, metric, node -> 0,
                        new ShortestPaths.Restrictions(bannedNodes, bannedEdges));
                if (!spur.found()) {
                    continue;
                }
                List<Integer> path = new ArrayList<>(root.subList(0, i));
                path.addAll(spur.nodes());
                if (known.add(path)) {
                    candidates.add(new PathResult(path, ShortestPaths.pathCost(graph, path, metric), spur.nodesExpanded()));
                }
            }
            if (candidates.isEmpty()) {
                break; // fewer than k loopless paths exist
            }
            accepted.add(candidates.poll());
        }
        return accepted;
    }
}
