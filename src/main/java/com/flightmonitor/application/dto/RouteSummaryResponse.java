package com.flightmonitor.application.dto;

import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.TransportType;

import java.time.LocalDate;
import java.util.List;

public record RouteSummaryResponse(
        Long routeId,
        String origin,
        String destination,
        LocalDate travelDate,
        List<SegmentSummary> segments
) {
    public record SegmentSummary(
            Long segmentId,
            TransportType transportType,
            String label,
            Money latestPrice,
            Money lowestPrice,
            Money purchasedPrice   // null if no record is marked as purchased
    ) {}
}
