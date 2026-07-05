package com.flightmonitor.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public class Route {

    private final Long id;
    private final Long userId;
    private final String origin;
    private final String destination;
    private final LocalDate travelDate;
    private final RouteStatus status;

    public Route(Long userId, String origin, String destination, LocalDate travelDate) {
        this(null, userId, origin, destination, travelDate, RouteStatus.WATCHING);
    }

    public Route(Long id, Long userId, String origin, String destination, LocalDate travelDate) {
        this(id, userId, origin, destination, travelDate, RouteStatus.WATCHING);
    }

    public Route(Long id, Long userId, String origin, String destination, LocalDate travelDate, RouteStatus status) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(origin, "Origin must not be null");
        Objects.requireNonNull(destination, "Destination must not be null");
        Objects.requireNonNull(travelDate, "Travel date must not be null");
        Objects.requireNonNull(status, "Status must not be null");

        if (origin.isBlank()) throw new IllegalArgumentException("Origin must not be blank");
        if (destination.isBlank()) throw new IllegalArgumentException("Destination must not be blank");
        if (origin.equalsIgnoreCase(destination))
            throw new IllegalArgumentException("Origin and destination must be different");

        this.id          = id;
        this.userId      = userId;
        this.origin      = origin.toUpperCase().trim();
        this.destination = destination.toUpperCase().trim();
        this.travelDate  = travelDate;
        this.status      = status;
    }

    public Long getId()              { return id; }
    public Long getUserId()          { return userId; }
    public String getOrigin()        { return origin; }
    public String getDestination()   { return destination; }
    public LocalDate getTravelDate() { return travelDate; }
    public RouteStatus getStatus()   { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Route{id=" + id + ", userId=" + userId
                + ", origin='" + origin + "', destination='" + destination
                + "', travelDate=" + travelDate + ", status=" + status + "}";
    }
}
