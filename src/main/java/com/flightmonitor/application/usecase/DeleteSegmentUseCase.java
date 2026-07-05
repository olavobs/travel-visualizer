package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.exception.PriceRecordNotFoundException;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.port.PriceCachePort;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteSegmentUseCase {

    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;
    private final PriceCachePort priceCachePort;

    public DeleteSegmentUseCase(RouteRepository routeRepository,
                                SegmentRepository segmentRepository,
                                PriceCachePort priceCachePort) {
        this.routeRepository   = routeRepository;
        this.segmentRepository = segmentRepository;
        this.priceCachePort    = priceCachePort;
    }

    public void execute(Long routeId, Long segmentId, Long userId) {
        if (!routeRepository.existsByIdAndUserId(routeId, userId)) {
            throw new RouteNotFoundException(routeId);
        }
        if (!segmentRepository.existsByIdAndRouteId(segmentId, routeId)) {
            throw new PriceRecordNotFoundException(segmentId);
        }
        priceCachePort.evict(segmentId);
        segmentRepository.deleteById(segmentId);
    }
}
