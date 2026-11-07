package com.kietnguyen.routefinder.place;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class PlaceDtos {

    private PlaceDtos() {
    }

    public record PlaceResponse(long id, String name, String category, double latitude, double longitude) {
        public static PlaceResponse of(Place p) {
            return new PlaceResponse(p.getId(), p.getName(), p.getCategory(), p.getLatitude(), p.getLongitude());
        }
    }

    public record NearbyPlace(PlaceResponse place, long distanceMeters) {
    }

    public record CreatePlaceRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 40) String category,
            @DecimalMin("-90") @DecimalMax("90") double latitude,
            @DecimalMin("-180") @DecimalMax("180") double longitude) {
    }
}
