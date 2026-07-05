package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.PriceRecord;

import java.time.Instant;

public record PriceRecordHttpResponse(Long id, Long segmentId, Money money, Instant recordedAt, boolean purchased) {

    public static PriceRecordHttpResponse from(PriceRecord record) {
        return new PriceRecordHttpResponse(
                record.getId(),
                record.getSegmentId(),
                record.getPrice(),
                record.getRecordedAt(),
                record.isPurchased()
        );
    }
}
