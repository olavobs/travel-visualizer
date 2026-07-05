package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.UpdatePriceRequest;
import com.flightmonitor.domain.exception.PriceRecordNotFoundException;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.domain.port.PriceCachePort;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdatePriceRecordUseCase {

    private final PriceRecordRepository priceRecordRepository;
    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;
    private final PriceCachePort priceCachePort;

    public UpdatePriceRecordUseCase(PriceRecordRepository priceRecordRepository,
                                    RouteRepository routeRepository,
                                    SegmentRepository segmentRepository,
                                    PriceCachePort priceCachePort) {
        this.priceRecordRepository = priceRecordRepository;
        this.routeRepository       = routeRepository;
        this.segmentRepository     = segmentRepository;
        this.priceCachePort        = priceCachePort;
    }

    public PriceRecord execute(UpdatePriceRequest request) {
        if (!routeRepository.existsByIdAndUserId(request.routeId(), request.userId())) {
            throw new RouteNotFoundException(request.routeId());
        }
        if (!segmentRepository.existsByIdAndRouteId(request.segmentId(), request.routeId())) {
            throw new PriceRecordNotFoundException(request.segmentId());
        }
        PriceRecord existing = priceRecordRepository.findById(request.priceId())
                .orElseThrow(() -> new PriceRecordNotFoundException(request.priceId()));
        if (!existing.getSegmentId().equals(request.segmentId())) {
            throw new PriceRecordNotFoundException(request.priceId());
        }
        PriceRecord updated = new PriceRecord(existing.getId(), request.segmentId(), request.price(), request.recordedAt(), existing.isPurchased());
        PriceRecord saved = priceRecordRepository.update(updated);
        priceCachePort.evict(request.segmentId());
        return saved;
    }
}
