package com.kietnguyen.routefinder.routing;

import com.kietnguyen.routefinder.algo.Graph;
import com.kietnguyen.routefinder.place.Place;
import com.kietnguyen.routefinder.place.PlaceRepository;
import com.kietnguyen.routefinder.road.RoadEdge;
import com.kietnguyen.routefinder.road.RoadRepository;
import com.kietnguyen.routefinder.web.ApiException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the road graph in memory. Loading it costs two queries; a route search then touches no
 * database at all.
 *
 * <p>The graph is immutable once built. Writers call {@link #invalidate()}, which drops the cached
 * graph after their transaction commits, and the next reader rebuilds it. Readers that already hold
 * the old snapshot keep using it safely, so no locking is needed on the read path.
 */
@Component
public class GraphCache {

    /** The graph plus the mapping between database ids and graph node numbers. */
    public record Snapshot(Graph graph, Map<Long, Integer> nodeOf, List<Place> placeAt) {
        public int node(long placeId) {
            Integer node = nodeOf.get(placeId);
            if (node == null) {
                throw ApiException.notFound("No place with id " + placeId);
            }
            return node;
        }
    }

    private final PlaceRepository places;
    private final RoadRepository roads;
    private final TransactionTemplate readOnly;
    private volatile Snapshot current;

    public GraphCache(PlaceRepository places, RoadRepository roads, PlatformTransactionManager transactions) {
        this.places = places;
        this.roads = roads;
        this.readOnly = new TransactionTemplate(transactions);
        this.readOnly.setReadOnly(true);
    }

    public Snapshot get() {
        Snapshot s = current;
        if (s == null) {
            synchronized (this) {
                s = current;
                if (s == null) { // double-checked: only one thread rebuilds
                    s = readOnly.execute(status -> build());
                    current = s;
                }
            }
        }
        return s;
    }

    public void invalidate() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // Drop the cache only after commit; dropping earlier could let a reader rebuild from
            // data that does not yet include this change.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    current = null;
                }
            });
        } else {
            current = null;
        }
    }

    private Snapshot build() {
        List<Place> all = places.findAll();
        Map<Long, Integer> nodeOf = new HashMap<>();
        Graph graph = new Graph(all.size());
        for (int i = 0; i < all.size(); i++) {
            Place p = all.get(i);
            nodeOf.put(p.getId(), i);
            graph.setLocation(i, p.getLatitude(), p.getLongitude());
        }
        for (RoadEdge r : roads.findAllEdges()) {
            int a = nodeOf.get(r.fromId());
            int b = nodeOf.get(r.toId());
            graph.addEdge(a, b, r.distanceMeters(), r.speedKmh(), r.name());
            if (!r.oneWay()) {
                graph.addEdge(b, a, r.distanceMeters(), r.speedKmh(), r.name());
            }
        }
        return new Snapshot(graph, Map.copyOf(nodeOf), List.copyOf(all));
    }
}
