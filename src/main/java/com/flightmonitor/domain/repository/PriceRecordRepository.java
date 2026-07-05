package com.flightmonitor.domain.repository;

import com.flightmonitor.domain.model.PriceRecord;

import java.util.List;
import java.util.Optional;

public interface PriceRecordRepository {

    PriceRecord save(PriceRecord record);

    List<PriceRecord> findBySegmentId(Long segmentId);

    Optional<PriceRecord> findLowestBySegmentId(Long segmentId);

    Optional<PriceRecord> findLatestBySegmentId(Long segmentId);

    Optional<PriceRecord> findById(Long id);

    void deleteById(Long id);

    PriceRecord update(PriceRecord record);

    Optional<PriceRecord> findPurchasedBySegmentId(Long segmentId);

    void markAsPurchased(Long priceId, Long segmentId);

    void unmarkAsPurchased(Long priceId);
}
