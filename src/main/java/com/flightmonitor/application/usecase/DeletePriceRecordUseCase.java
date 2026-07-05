package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.exception.PriceRecordNotFoundException;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.domain.port.PriceCachePort;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

@Service
public class DeletePriceRecordUseCase {

    private final PriceRecordRepository priceRecordRepository;
    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;
    private final PriceCachePort priceCachePort;

    public DeletePriceRecordUseCase(PriceRecordRepository priceRecordRepository,
                                    RouteRepository routeRepository,
                                    SegmentRepository segmentRepository,
                                    PriceCachePort priceCachePort) {
        this.priceRecordRepository = priceRecordRepository;
        this.routeRepository       = routeRepository;
        this.segmentRepository     = segmentRepository;
        this.priceCachePort        = priceCachePort;
    }

    public void execute(Long routeId, Long segmentId, Long priceId, Long userId) {
        if (!routeRepository.existsByIdAndUserId(routeId, userId)) {
            throw new RouteNotFoundException(routeId);
        }
        if (!segmentRepository.existsByIdAndRouteId(segmentId, routeId)) {
            throw new PriceRecordNotFoundException(segmentId);
        }
        PriceRecord record = priceRecordRepository.findById(priceId)
                .orElseThrow(() -> new PriceRecordNotFoundException(priceId));
        if (!record.getSegmentId().equals(segmentId)) {
            throw new PriceRecordNotFoundException(priceId);
        }
        priceRecordRepository.deleteById(priceId);
        priceCachePort.evict(segmentId);
    }
}
