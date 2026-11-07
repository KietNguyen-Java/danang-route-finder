package com.kietnguyen.routefinder.algo;

import java.util.Arrays;

/**
 * Optimal visiting order for a small set of stops (the travelling salesman problem), solved
 * exactly with the Held-Karp dynamic program.
 *
 * <p>{@code best[mask][j]} is the cheapest way to leave stop 0, visit exactly the stops in
 * {@code mask}, and end at stop {@code j}. Each state extends to one more unvisited stop.
 * O(2^n · n^2) time and O(2^n · n) memory, versus O(n!) for trying every order:
 * for 12 stops after the start that is about 600,000 steps instead of 12! = 479 million orders.
 */
public final class TourPlanner {

    public static final int MAX_STOPS = 13;

    /** {@code order} lists indices into the cost matrix, starting with 0. */
    public record Tour(int[] order, double cost) {
        public boolean found() {
            return Double.isFinite(cost);
        }
    }

    private TourPlanner() {
    }

    /**
     * @param cost          {@code cost[a][b]} is the cheapest route from stop a to stop b, or infinity
     * @param returnToStart whether the tour must end back at stop 0
     */
    public static Tour solve(double[][] cost, boolean returnToStart) {
        int n = cost.length;
        if (n == 0 || n > MAX_STOPS) {
            throw new IllegalArgumentException("between 1 and " + MAX_STOPS + " stops are supported, got " + n);
        }
        if (n == 1) {
            return new Tour(new int[]{0}, 0);
        }
        int m = n - 1; // stop 0 is fixed as the start; bit j stands for stop j + 1
        int full = (1 << m) - 1;
        double[][] best = new double[1 << m][m];
        int[][] parent = new int[1 << m][m];
        for (double[] row : best) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }
        for (int j = 0; j < m; j++) {
            best[1 << j][j] = cost[0][j + 1];
            parent[1 << j][j] = -1;
        }

        for (int mask = 1; mask <= full; mask++) {
            for (int j = 0; j < m; j++) {
                if ((mask & (1 << j)) == 0 || Double.isInfinite(best[mask][j])) {
                    continue;
                }
                for (int next = 0; next < m; next++) {
                    if ((mask & (1 << next)) != 0) {
                        continue;
                    }
                    int nextMask = mask | (1 << next);
                    double candidate = best[mask][j] + cost[j + 1][next + 1];
                    if (candidate < best[nextMask][next]) {
                        best[nextMask][next] = candidate;
                        parent[nextMask][next] = j;
                    }
                }
            }
        }

        double bestCost = Double.POSITIVE_INFINITY;
        int last = -1;
        for (int j = 0; j < m; j++) {
            double total = best[full][j] + (returnToStart ? cost[j + 1][0] : 0);
            if (total < bestCost) {
                bestCost = total;
                last = j;
            }
        }
        if (last < 0) {
            return new Tour(new int[0], Double.POSITIVE_INFINITY);
        }

        int[] order = new int[n];
        order[0] = 0;
        int mask = full;
        for (int pos = n - 1, j = last; pos >= 1; pos--) {
            order[pos] = j + 1;
            int previous = parent[mask][j];
            mask &= ~(1 << j);
            j = previous;
        }
        return new Tour(order, bestCost);
    }
}
