package com.kietnguyen.routefinder.road;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class RoadDtos {

    private RoadDtos() {
    }

    public record RoadResponse(long id, long fromPlaceId, String fromPlace, long toPlaceId, String toPlace,
                               String name, int distanceMeters, int speedKmh, boolean oneWay) {
        public static RoadResponse of(Road r) {
            return new RoadResponse(r.getId(), r.getFrom().getId(), r.getFrom().getName(), r.getTo().getId(),
                    r.getTo().getName(), r.getName(), r.getDistanceMeters(), r.getSpeedKmh(), r.isOneWay());
        }
    }

    public record CreateRoadRequest(
            @NotNull Long fromPlaceId,
            @NotNull Long toPlaceId,
            @Size(max = 120) String name,
            @NotNull @Positive Integer distanceMeters,
            @NotNull @Min(5) @Max(120) Integer speedKmh,
            boolean oneWay) {
    }
}
