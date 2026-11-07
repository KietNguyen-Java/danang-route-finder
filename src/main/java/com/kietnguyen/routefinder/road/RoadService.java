package com.kietnguyen.routefinder.road;

import com.kietnguyen.routefinder.algo.Geo;
import com.kietnguyen.routefinder.place.Place;
import com.kietnguyen.routefinder.place.PlaceRepository;
import com.kietnguyen.routefinder.road.RoadDtos.CreateRoadRequest;
import com.kietnguyen.routefinder.road.RoadDtos.RoadResponse;
import com.kietnguyen.routefinder.routing.GraphCache;
import com.kietnguyen.routefinder.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoadService {

    private final RoadRepository roads;
    private final PlaceRepository places;
    private final GraphCache graphCache;

    public RoadService(RoadRepository roads, PlaceRepository places, GraphCache graphCache) {
        this.roads = roads;
        this.places = places;
        this.graphCache = graphCache;
    }

    @Transactional(readOnly = true)
    public List<RoadResponse> list() {
        return roads.findAllWithPlaces().stream().map(RoadResponse::of).toList();
    }

    @Transactional
    public RoadResponse create(CreateRoadRequest request) {
        if (request.fromPlaceId().equals(request.toPlaceId())) {
            throw ApiException.badRequest("A road must connect two different places");
        }
        Place from = places.findById(request.fromPlaceId())
                .orElseThrow(() -> ApiException.notFound("No place with id " + request.fromPlaceId()));
        Place to = places.findById(request.toPlaceId())
                .orElseThrow(() -> ApiException.notFound("No place with id " + request.toPlaceId()));

        // A road shorter than the straight line is physically impossible, and it would make the
        // A* heuristic overestimate, so A* could return a wrong route. Reject it at the door.
        long straightLine = (long) Math.floor(Geo.distanceMeters(
                from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude()));
        if (request.distanceMeters() < straightLine) {
            throw ApiException.badRequest("Road length " + request.distanceMeters()
                    + " m is shorter than the straight-line distance " + straightLine + " m");
        }
        if (roads.existsByFromIdAndToId(from.getId(), to.getId())
                || (!request.oneWay() && roads.existsByFromIdAndToId(to.getId(), from.getId()))) {
            throw ApiException.conflict("A road between these places already exists");
        }

        Road saved = roads.save(new Road(from, to, request.name(), request.distanceMeters(),
                request.speedKmh(), request.oneWay()));
        graphCache.invalidate();
        return RoadResponse.of(saved);
    }
}
