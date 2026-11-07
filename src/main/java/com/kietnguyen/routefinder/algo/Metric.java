package com.kietnguyen.routefinder.algo;

/** What a route minimizes. */
public enum Metric {
    DISTANCE {
        @Override
        public double weight(Graph.Edge edge) {
            return edge.meters();
        }

        @Override
        public double lowerBound(Graph graph, int from, int to) {
            return graph.straightLineMeters(from, to);
        }
    },
    TIME {
        @Override
        public double weight(Graph.Edge edge) {
            return edge.seconds();
        }

        @Override
        public double lowerBound(Graph graph, int from, int to) {
            double speed = graph.maxSpeedMps();
            return speed == 0 ? 0 : graph.straightLineMeters(from, to) / speed;
        }
    };

    public abstract double weight(Graph.Edge edge);

    /**
     * A value never larger than the true cost from {@code from} to {@code to}. This holds because
     * every road is at least as long as the straight line between its ends, and no road is faster
     * than the fastest road. A* uses it as its heuristic.
     */
    public abstract double lowerBound(Graph graph, int from, int to);
}
