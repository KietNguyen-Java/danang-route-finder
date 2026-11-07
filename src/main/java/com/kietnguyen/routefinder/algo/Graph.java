package com.kietnguyen.routefinder.algo;

import java.util.ArrayList;
import java.util.List;

/**
 * A directed road graph stored as adjacency lists. Nodes are numbered {@code 0..size-1};
 * the service layer maps database ids to these numbers.
 */
public final class Graph {

    public record Edge(int from, int to, double meters, double seconds, String label) {
    }

    private final double[] latitude;
    private final double[] longitude;
    private final List<List<Edge>> outgoing;
    private int edgeCount;
    private double maxSpeedMps;

    public Graph(int size) {
        latitude = new double[size];
        longitude = new double[size];
        outgoing = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            outgoing.add(new ArrayList<>());
        }
    }

    public int size() {
        return outgoing.size();
    }

    public int edgeCount() {
        return edgeCount;
    }

    public void setLocation(int node, double lat, double lon) {
        latitude[node] = lat;
        longitude[node] = lon;
    }

    public void addEdge(int from, int to, double meters, double speedKmh) {
        addEdge(from, to, meters, speedKmh, null);
    }

    public void addEdge(int from, int to, double meters, double speedKmh, String label) {
        double speedMps = speedKmh / 3.6;
        outgoing.get(from).add(new Edge(from, to, meters, meters / speedMps, label));
        maxSpeedMps = Math.max(maxSpeedMps, speedMps);
        edgeCount++;
    }

    public List<Edge> edgesFrom(int node) {
        return outgoing.get(node);
    }

    public double straightLineMeters(int a, int b) {
        return Geo.distanceMeters(latitude[a], longitude[a], latitude[b], longitude[b]);
    }

    /** The fastest speed on any road; used to turn a distance into a lower bound on travel time. */
    public double maxSpeedMps() {
        return maxSpeedMps;
    }
}
