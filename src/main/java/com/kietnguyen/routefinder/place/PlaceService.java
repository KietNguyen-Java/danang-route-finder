package com.kietnguyen.routefinder.place;

import com.kietnguyen.routefinder.algo.Geo;
import com.kietnguyen.routefinder.place.PlaceDtos.CreatePlaceRequest;
import com.kietnguyen.routefinder.place.PlaceDtos.NearbyPlace;
import com.kietnguyen.routefinder.place.PlaceDtos.PlaceResponse;
import com.kietnguyen.routefinder.routing.GraphCache;
import com.kietnguyen.routefinder.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PlaceService {

    private final PlaceRepository places;
    private final GraphCache graphCache;

    public PlaceService(PlaceRepository places, GraphCache graphCache) {
        this.places = places;
        this.graphCache = graphCache;
    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> list(String category) {
        List<Place> found = category == null
                ? places.findAllByOrderByNameAsc()
                : places.findByCategoryIgnoreCaseOrderByNameAsc(category);
        return found.stream().map(PlaceResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public PlaceResponse get(long id) {
        return PlaceResponse.of(require(id));
    }

    @Transactional
    public PlaceResponse create(CreatePlaceRequest request) {
        if (places.existsByNameIgnoreCase(request.name())) {
            throw ApiException.conflict("A place named '" + request.name() + "' already exists");
        }
        Place saved = places.save(new Place(request.name().trim(), request.category().trim().toUpperCase(),
                request.latitude(), request.longitude()));
        graphCache.invalidate();
        return PlaceResponse.of(saved);
    }

    /**
     * Places within {@code radiusMeters} of a point, nearest first. Two steps: an indexed
     * bounding-box query in the database, then the exact haversine distance in Java.
     */
    @Transactional(readOnly = true)
    public List<NearbyPlace> nearby(double latitude, double longitude, double radiusMeters, int limit) {
        double dLat = Geo.latitudeDelta(radiusMeters);
        double dLon = Geo.longitudeDelta(radiusMeters, latitude);
        return places.findInBox(latitude - dLat, latitude + dLat, longitude - dLon, longitude + dLon).stream()
                .map(p -> new NearbyPlace(PlaceResponse.of(p),
                        Math.round(Geo.distanceMeters(latitude, longitude, p.getLatitude(), p.getLongitude()))))
                .filter(n -> n.distanceMeters() <= radiusMeters) // the box's corners are outside the circle
                .sorted(Comparator.comparingLong(NearbyPlace::distanceMeters))
                .limit(limit)
                .toList();
    }

    Place require(long id) {
        return places.findById(id).orElseThrow(() -> ApiException.notFound("No place with id " + id));
    }
}
