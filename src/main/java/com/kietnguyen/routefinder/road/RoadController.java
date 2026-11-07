package com.kietnguyen.routefinder.road;

import com.kietnguyen.routefinder.road.RoadDtos.CreateRoadRequest;
import com.kietnguyen.routefinder.road.RoadDtos.RoadResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roads")
public class RoadController {

    private final RoadService service;

    public RoadController(RoadService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoadResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoadResponse create(@Valid @RequestBody CreateRoadRequest request) {
        return service.create(request);
    }
}
