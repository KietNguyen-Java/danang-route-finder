package com.kietnguyen.routefinder.road;

import com.kietnguyen.routefinder.place.Place;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "road")
public class Road {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_place_id")
    private Place from;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_place_id")
    private Place to;

    @Column(length = 120)
    private String name;

    @Column(name = "distance_m", nullable = false)
    private int distanceMeters;

    @Column(name = "speed_kmh", nullable = false)
    private int speedKmh;

    @Column(name = "one_way", nullable = false)
    private boolean oneWay;

    protected Road() {
        // for JPA
    }

    public Road(Place from, Place to, String name, int distanceMeters, int speedKmh, boolean oneWay) {
        this.from = from;
        this.to = to;
        this.name = name;
        this.distanceMeters = distanceMeters;
        this.speedKmh = speedKmh;
        this.oneWay = oneWay;
    }

    public Long getId() {
        return id;
    }

    public Place getFrom() {
        return from;
    }

    public Place getTo() {
        return to;
    }

    public String getName() {
        return name;
    }

    public int getDistanceMeters() {
        return distanceMeters;
    }

    public int getSpeedKmh() {
        return speedKmh;
    }

    public boolean isOneWay() {
        return oneWay;
    }
}
