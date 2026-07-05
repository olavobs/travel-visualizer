package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.domain.model.PriceRecord;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceRecordHttpResponse(Long id, Long segmentId, BigDecimal price, String currency, LocalDate recordedDate, boolean purchased) {

    public static PriceRecordHttpResponse from(PriceRecord record) {
        return new PriceRecordHttpResponse(
                record.getId(),
                record.getSegmentId(),
                record.getPrice().amount(),
                record.getPrice().currency().name(),
                record.getRecordedDate(),
                record.isPurchased()
        );
    }
}
