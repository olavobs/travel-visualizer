package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.application.dto.RouteSummaryResponse;
import com.flightmonitor.application.dto.RouteSummaryResponse.SegmentSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RouteSummaryHttpResponse(
        Long routeId,
        String origin,
        String destination,
        LocalDate travelDate,
        List<SegmentSummaryItem> segments
) {
    public static RouteSummaryHttpResponse from(RouteSummaryResponse result) {
        return new RouteSummaryHttpResponse(
                result.routeId(),
                result.origin(),
                result.destination(),
                result.travelDate(),
                result.segments().stream().map(SegmentSummaryItem::from).toList()
        );
    }

    public record SegmentSummaryItem(
            Long segmentId,
            String transportType,
            String label,
            BigDecimal latestPrice,
            String latestCurrency,
            BigDecimal lowestPrice,
            String lowestCurrency,
            BigDecimal purchasedPrice,
            String purchasedCurrency
    ) {
        static SegmentSummaryItem from(SegmentSummary s) {
            return new SegmentSummaryItem(
                    s.segmentId(),
                    s.transportType().name(),
                    s.label(),
                    s.latestPrice() != null ? s.latestPrice().amount() : null,
                    s.latestPrice() != null ? s.latestPrice().currency().name() : null,
                    s.lowestPrice() != null ? s.lowestPrice().amount() : null,
                    s.lowestPrice() != null ? s.lowestPrice().currency().name() : null,
                    s.purchasedPrice() != null ? s.purchasedPrice().amount() : null,
                    s.purchasedPrice() != null ? s.purchasedPrice().currency().name() : null
            );
        }
    }
}
