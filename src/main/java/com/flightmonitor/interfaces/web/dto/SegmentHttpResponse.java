package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.domain.model.Segment;

public record SegmentHttpResponse(Long id, Long routeId, String transportType, String label) {

    public static SegmentHttpResponse from(Segment segment) {
        return new SegmentHttpResponse(
                segment.getId(),
                segment.getRouteId(),
                segment.getTransportType().name(),
                segment.getLabel()
        );
    }
}
