package com.kietnguyen.routefinder.road;

/** A road as the routing graph sees it: a projection, not an entity. */
public record RoadEdge(long fromId, long toId, String name, int distanceMeters, int speedKmh, boolean oneWay) {
}
