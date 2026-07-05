package com.flightmonitor.domain.model;

import java.time.Instant;
import java.util.Objects;

public class PriceRecord {

    private final Long id;
    private final Long segmentId;
    private final Money price;
    private final Instant recordedAt;
    private final boolean purchased;

    public PriceRecord(Long segmentId, Money price, Instant recordedAt) {
        this(null, segmentId, price, recordedAt, false);
    }

    public PriceRecord(Long id, Long segmentId, Money price, Instant recordedAt) {
        this(id, segmentId, price, recordedAt, false);
    }

    public PriceRecord(Long id, Long segmentId, Money price, Instant recordedAt, boolean purchased) {
        Objects.requireNonNull(segmentId, "Segment ID must not be null");
        Objects.requireNonNull(price, "Price must not be null");
        Objects.requireNonNull(recordedAt, "Recorded instant must not be null");

        this.id         = id;
        this.segmentId  = segmentId;
        this.price      = price;
        this.recordedAt = recordedAt;
        this.purchased  = purchased;
    }

    public Long getId()            { return id; }
    public Long getSegmentId()     { return segmentId; }
    public Money getPrice()        { return price; }
    public Instant getRecordedAt() { return recordedAt; }
    public boolean isPurchased()   { return purchased; }

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
                + ", price=" + price + ", recordedAt=" + recordedAt
                + ", purchased=" + purchased + "}";
    }
}
