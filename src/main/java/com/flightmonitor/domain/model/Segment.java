package com.flightmonitor.domain.model;

import java.util.Objects;

public class Segment {

    private final Long id;
    private final Long routeId;
    private final TransportType transportType;
    private final String label;

    public Segment(Long routeId, TransportType transportType, String label) {
        this(null, routeId, transportType, label);
    }

    public Segment(Long id, Long routeId, TransportType transportType, String label) {
        Objects.requireNonNull(routeId, "Route ID must not be null");
        Objects.requireNonNull(transportType, "Transport type must not be null");
        this.id = id;
        this.routeId = routeId;
        this.transportType = transportType;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public Long getRouteId() {
        return routeId;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Segment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Segment{id=" + id + ", routeId=" + routeId
                + ", transportType=" + transportType + ", label='" + label + "'}";
    }
}
