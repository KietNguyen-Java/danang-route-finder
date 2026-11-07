package com.kietnguyen.routefinder.routing;

import com.kietnguyen.routefinder.algo.Graph;
import com.kietnguyen.routefinder.algo.KShortestPaths;
import com.kietnguyen.routefinder.algo.Metric;
import com.kietnguyen.routefinder.algo.PathResult;
import com.kietnguyen.routefinder.algo.ShortestPaths;
import com.kietnguyen.routefinder.algo.TourPlanner;
import com.kietnguyen.routefinder.place.PlaceDtos.PlaceResponse;
import com.kietnguyen.routefinder.routing.RouteDtos.Algorithm;
import com.kietnguyen.routefinder.routing.RouteDtos.AlgorithmRun;
import com.kietnguyen.routefinder.routing.RouteDtos.ComparisonResponse;
import com.kietnguyen.routefinder.routing.RouteDtos.RouteResponse;
import com.kietnguyen.routefinder.routing.RouteDtos.Step;
import com.kietnguyen.routefinder.routing.RouteDtos.TourRequest;
import com.kietnguyen.routefinder.routing.RouteDtos.TourResponse;
import com.kietnguyen.routefinder.web.ApiException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
public class RoutingService {

    private final GraphCache cache;

    public RoutingService(GraphCache cache) {
        this.cache = cache;
    }

    public RouteResponse route(long fromId, long toId, Algorithm algorithm, Metric metric) {
        GraphCache.Snapshot s = cache.get();
        int from = s.node(fromId);
        int to = s.node(toId);
        PathResult path = switch (algorithm) {
            case DIJKSTRA -> ShortestPaths.dijkstra(s.graph(), from, to, metric);
            case ASTAR -> ShortestPaths.aStar(s.graph(), from, to, metric);
            case BFS -> ShortestPaths.fewestRoads(s.graph(), from, to);
        };
        if (!path.found()) {
            throw ApiException.notFound("No route from " + name(s, from) + " to " + name(s, to));
        }
        return describe(s, path, algorithm.name(), metric);
    }

    public List<RouteResponse> alternatives(long fromId, long toId, int k, Metric metric) {
        GraphCache.Snapshot s = cache.get();
        int from = s.node(fromId);
        int to = s.node(toId);
        List<PathResult> paths = KShortestPaths.find(s.graph(), from, to, k, metric);
        if (paths.isEmpty()) {
            throw ApiException.notFound("No route from " + name(s, from) + " to " + name(s, to));
        }
        return paths.stream().map(p -> describe(s, p, "YEN", metric)).toList();
    }

    /** Runs Dijkstra and A* on the same query and reports how much work each did. */
    public ComparisonResponse compare(long fromId, long toId, Metric metric) {
        GraphCache.Snapshot s = cache.get();
        int from = s.node(fromId);
        int to = s.node(toId);
        List<AlgorithmRun> runs = List.of(
                timed("DIJKSTRA", () -> ShortestPaths.dijkstra(s.graph(), from, to, metric)),
                timed("ASTAR", () -> ShortestPaths.aStar(s.graph(), from, to, metric)));
        return new ComparisonResponse(place(s, from), place(s, to), metric, runs);
    }

    /**
     * Best order to visit every stop. First the cheapest route between every pair of points
     * (one Dijkstra per point), then the Held-Karp dynamic program over that cost matrix.
     */
    public TourResponse tour(TourRequest request) {
        Metric metric = request.metric() == null ? Metric.DISTANCE : request.metric();
        GraphCache.Snapshot s = cache.get();

        Set<Long> ids = new LinkedHashSet<>();
        ids.add(request.startPlaceId());
        ids.addAll(request.stopPlaceIds());
        if (ids.size() < 2) {
            throw ApiException.badRequest("Give at least one stop other than the start");
        }
        int[] points = ids.stream().mapToInt(s::node).toArray();

        double[][] cost = new double[points.length][];
        for (int i = 0; i < points.length; i++) {
            double[] all = ShortestPaths.distancesFrom(s.graph(), points[i], metric);
            cost[i] = new double[points.length];
            for (int j = 0; j < points.length; j++) {
                cost[i][j] = all[points[j]];
            }
        }
        TourPlanner.Tour tour = TourPlanner.solve(cost, request.returnToStart());
        if (!tour.found()) {
            throw ApiException.notFound("Some stops cannot be reached from each other");
        }

        List<Integer> stops = new ArrayList<>();
        for (int i : tour.order()) {
            stops.add(points[i]);
        }
        if (request.returnToStart()) {
            stops.add(points[0]);
        }
        List<RouteResponse> legs = new ArrayList<>();
        for (int i = 0; i + 1 < stops.size(); i++) {
            legs.add(describe(s, ShortestPaths.dijkstra(s.graph(), stops.get(i), stops.get(i + 1), metric), "DIJKSTRA", metric));
        }
        List<PlaceResponse> order = stops.stream().map(n -> place(s, n)).toList();
        return new TourResponse(metric, order,
                legs.stream().mapToLong(RouteResponse::totalDistanceMeters).sum(),
                legs.stream().mapToLong(RouteResponse::totalSeconds).sum(),
                legs);
    }

    private RouteResponse describe(GraphCache.Snapshot s, PathResult path, String algorithm, Metric metric) {
        List<Integer> nodes = path.nodes();
        List<Step> steps = new ArrayList<>();
        double meters = 0;
        double seconds = 0;
        for (int i = 0; i + 1 < nodes.size(); i++) {
            int a = nodes.get(i);
            int b = nodes.get(i + 1);
            // BFS ignores road lengths; each hop is described by the cheapest road in the requested metric.
            Graph.Edge e = ShortestPaths.cheapestEdge(s.graph(), a, b, metric).orElseThrow();
            meters += e.meters();
            seconds += e.seconds();
            steps.add(new Step(place(s, a), place(s, b), e.label(), Math.round(e.meters()), Math.round(e.seconds())));
        }
        return new RouteResponse(algorithm, metric, Math.round(meters), Math.round(seconds), path.nodesExpanded(),
                nodes.stream().map(n -> place(s, n)).toList(), steps);
    }

    private static AlgorithmRun timed(String name, Supplier<PathResult> search) {
        long start = System.nanoTime();
        PathResult result = search.get();
        long micros = (System.nanoTime() - start) / 1_000;
        return new AlgorithmRun(name, result.cost(), result.nodesExpanded(), micros);
    }

    private static PlaceResponse place(GraphCache.Snapshot s, int node) {
        return PlaceResponse.of(s.placeAt().get(node));
    }

    private static String name(GraphCache.Snapshot s, int node) {
        return s.placeAt().get(node).getName();
    }
}
