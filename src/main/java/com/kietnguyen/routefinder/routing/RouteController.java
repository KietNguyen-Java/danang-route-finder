package com.kietnguyen.routefinder.routing;

import com.kietnguyen.routefinder.algo.Metric;
import com.kietnguyen.routefinder.routing.RouteDtos.Algorithm;
import com.kietnguyen.routefinder.routing.RouteDtos.ComparisonResponse;
import com.kietnguyen.routefinder.routing.RouteDtos.RouteResponse;
import com.kietnguyen.routefinder.routing.RouteDtos.TourRequest;
import com.kietnguyen.routefinder.routing.RouteDtos.TourResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RouteController {

    private final RoutingService service;

    public RouteController(RoutingService service) {
        this.service = service;
    }

    @GetMapping("/api/routes")
    public RouteResponse route(@RequestParam long from, @RequestParam long to,
                               @RequestParam(defaultValue = "ASTAR") Algorithm algorithm,
                               @RequestParam(defaultValue = "DISTANCE") Metric metric) {
        return service.route(from, to, algorithm, metric);
    }

    @GetMapping("/api/routes/alternatives")
    public List<RouteResponse> alternatives(@RequestParam long from, @RequestParam long to,
                                            @RequestParam(defaultValue = "3") @Min(1) @Max(10) int k,
                                            @RequestParam(defaultValue = "DISTANCE") Metric metric) {
        return service.alternatives(from, to, k, metric);
    }

    @GetMapping("/api/routes/compare")
    public ComparisonResponse compare(@RequestParam long from, @RequestParam long to,
                                      @RequestParam(defaultValue = "DISTANCE") Metric metric) {
        return service.compare(from, to, metric);
    }

    @PostMapping("/api/tours")
    public TourResponse tour(@Valid @RequestBody TourRequest request) {
        return service.tour(request);
    }
}
