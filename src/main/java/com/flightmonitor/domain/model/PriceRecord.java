package com.flightmonitor.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public class PriceRecord {

    private final Long id;
    private final Long segmentId;
    private final Money price;
    private final LocalDate recordedDate;
    private final boolean purchased;

    public PriceRecord(Long segmentId, Money price, LocalDate recordedDate) {
        this(null, segmentId, price, recordedDate, false);
    }

    public PriceRecord(Long id, Long segmentId, Money price, LocalDate recordedDate) {
        this(id, segmentId, price, recordedDate, false);
    }

    public PriceRecord(Long id, Long segmentId, Money price, LocalDate recordedDate, boolean purchased) {
        Objects.requireNonNull(segmentId, "Segment ID must not be null");
        Objects.requireNonNull(price, "Price must not be null");
        Objects.requireNonNull(recordedDate, "Recorded date must not be null");

        this.id           = id;
        this.segmentId    = segmentId;
        this.price        = price;
        this.recordedDate = recordedDate;
        this.purchased    = purchased;
    }

    public Long getId()                { return id; }
    public Long getSegmentId()         { return segmentId; }
    public Money getPrice()            { return price; }
    public LocalDate getRecordedDate() { return recordedDate; }
    public boolean isPurchased()       { return purchased; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceRecord that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "PriceRecord{id=" + id + ", segmentId=" + segmentId
                + ", price=" + price + ", recordedDate=" + recordedDate
                + ", purchased=" + purchased + "}";
    }
}
