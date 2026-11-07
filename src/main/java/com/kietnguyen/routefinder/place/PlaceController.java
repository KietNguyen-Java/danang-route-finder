package com.kietnguyen.routefinder.place;

import com.kietnguyen.routefinder.place.PlaceDtos.CreatePlaceRequest;
import com.kietnguyen.routefinder.place.PlaceDtos.NearbyPlace;
import com.kietnguyen.routefinder.place.PlaceDtos.PlaceResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService service;

    public PlaceController(PlaceService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlaceResponse> list(@RequestParam(required = false) String category) {
        return service.list(category);
    }

    @GetMapping("/{id}")
    public PlaceResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceResponse create(@Valid @RequestBody CreatePlaceRequest request) {
        return service.create(request);
    }

    @GetMapping("/nearby")
    public List<NearbyPlace> nearby(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lon,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(50_000) double radiusMeters,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return service.nearby(lat, lon, radiusMeters, limit);
    }
}
