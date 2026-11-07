package com.kietnguyen.routefinder.road;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoadRepository extends JpaRepository<Road, Long> {

    /** Just the columns the graph needs, in one query, without loading Road or Place entities. */
    @Query("""
            select new com.kietnguyen.routefinder.road.RoadEdge(
                r.from.id, r.to.id, r.name, r.distanceMeters, r.speedKmh, r.oneWay)
            from Road r
            """)
    List<RoadEdge> findAllEdges();

    /** Loads each road with both places in a single join, avoiding one query per road (the N+1 problem). */
    @Query("select r from Road r join fetch r.from join fetch r.to order by r.id")
    List<Road> findAllWithPlaces();

    boolean existsByFromIdAndToId(long fromId, long toId);
}
