package com.kietnguyen.routefinder.algo;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TourPlannerTest {

    @Test
    void findsTheObviousLoop() {
        // Four corners of a square: going around is 4, any diagonal costs more.
        double d = Math.sqrt(2);
        double[][] cost = {
                {0, 1, d, 1},
                {1, 0, 1, d},
                {d, 1, 0, 1},
                {1, d, 1, 0},
        };
        TourPlanner.Tour tour = TourPlanner.solve(cost, true);
        assertEquals(4, tour.cost(), 1e-9);
        assertEquals(0, tour.order()[0]);
    }

    @Test
    void matchesBruteForceOnRandomAsymmetricCosts() {
        Random random = new Random(5);
        for (int trial = 0; trial < 50; trial++) {
            int n = 2 + random.nextInt(6);
            double[][] cost = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    cost[i][j] = i == j ? 0 : 1 + random.nextInt(100);
                }
            }
            for (boolean loop : new boolean[]{true, false}) {
                TourPlanner.Tour tour = TourPlanner.solve(cost, loop);
                assertEquals(bruteForce(cost, loop), tour.cost(), 1e-9, "n=" + n + " loop=" + loop);
                assertEquals(tour.cost(), costOf(cost, tour.order(), loop), 1e-9, "order must match its cost");
                int[] sorted = tour.order().clone();
                Arrays.sort(sorted);
                int[] all = new int[n];
                Arrays.setAll(all, i -> i);
                assertArrayEquals(all, sorted, "every stop exactly once");
            }
        }
    }

    @Test
    void reportsUnreachableStops() {
        double inf = Double.POSITIVE_INFINITY;
        assertFalse(TourPlanner.solve(new double[][]{{0, inf}, {inf, 0}}, false).found());
    }

    @Test
    void limitsProblemSize() {
        assertThrows(IllegalArgumentException.class, () -> TourPlanner.solve(new double[TourPlanner.MAX_STOPS + 1][], true));
    }

    private static double costOf(double[][] cost, int[] order, boolean loop) {
        double total = 0;
        for (int i = 0; i + 1 < order.length; i++) {
            total += cost[order[i]][order[i + 1]];
        }
        return loop ? total + cost[order[order.length - 1]][order[0]] : total;
    }

    private static double bruteForce(double[][] cost, boolean loop) {
        int n = cost.length;
        int[] rest = new int[n - 1];
        Arrays.setAll(rest, i -> i + 1);
        double[] best = {Double.POSITIVE_INFINITY};
        permute(rest, 0, cost, loop, best);
        return best[0];
    }

    private static void permute(int[] a, int k, double[][] cost, boolean loop, double[] best) {
        if (k == a.length) {
            int[] order = new int[a.length + 1];
            System.arraycopy(a, 0, order, 1, a.length);
            best[0] = Math.min(best[0], costOf(cost, order, loop));
            return;
        }
        for (int i = k; i < a.length; i++) {
            int t = a[k]; a[k] = a[i]; a[i] = t;
            permute(a, k + 1, cost, loop, best);
            t = a[k]; a[k] = a[i]; a[i] = t;
        }
    }
}
