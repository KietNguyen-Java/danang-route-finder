package com.kietnguyen.routefinder.routing;

import com.kietnguyen.routefinder.algo.Metric;
import com.kietnguyen.routefinder.place.PlaceDtos.PlaceResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class RouteDtos {

    private RouteDtos() {
    }

    public enum Algorithm { DIJKSTRA, ASTAR, BFS }

    /** One road travelled, from one place to the next. */
    public record Step(PlaceResponse from, PlaceResponse to, String road, long distanceMeters, long seconds) {
    }

    public record RouteResponse(String algorithm, Metric metric, long totalDistanceMeters, long totalSeconds,
                                int nodesExpanded, List<PlaceResponse> places, List<Step> steps) {
    }

    public record AlgorithmRun(String algorithm, double cost, int nodesExpanded, long micros) {
    }

    public record ComparisonResponse(PlaceResponse from, PlaceResponse to, Metric metric, List<AlgorithmRun> runs) {
    }

    public record TourRequest(
            @NotNull Long startPlaceId,
            @NotEmpty @Size(max = 12) List<@NotNull Long> stopPlaceIds,
            boolean returnToStart,
            Metric metric) {
    }

    public record TourResponse(Metric metric, List<PlaceResponse> visitOrder, long totalDistanceMeters,
                               long totalSeconds, List<RouteResponse> legs) {
    }
}
