package com.kietnguyen.routefinder.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findAllByOrderByNameAsc();

    List<Place> findByCategoryIgnoreCaseOrderByNameAsc(String category);

    boolean existsByNameIgnoreCase(String name);

    /**
     * Places inside a latitude/longitude rectangle. This is the cheap prefilter for a radius
     * search: it can use the composite index on (latitude, longitude), and the exact circle
     * distance is then computed only for the few rows inside the box.
     */
    @Query("""
            select p from Place p
            where p.latitude between :minLat and :maxLat
              and p.longitude between :minLon and :maxLon
            """)
    List<Place> findInBox(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                          @Param("minLon") double minLon, @Param("maxLon") double maxLon);
}
