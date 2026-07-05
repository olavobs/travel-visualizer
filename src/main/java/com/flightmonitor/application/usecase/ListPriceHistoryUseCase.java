package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.exception.PriceRecordNotFoundException;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListPriceHistoryUseCase {

    private final PriceRecordRepository priceRecordRepository;
    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;

    public ListPriceHistoryUseCase(PriceRecordRepository priceRecordRepository,
                                   RouteRepository routeRepository,
                                   SegmentRepository segmentRepository) {
        this.priceRecordRepository = priceRecordRepository;
        this.routeRepository       = routeRepository;
        this.segmentRepository     = segmentRepository;
    }

    public List<PriceRecord> execute(Long routeId, Long segmentId, Long userId) {
        if (!routeRepository.existsByIdAndUserId(routeId, userId)) {
            throw new RouteNotFoundException(routeId);
        }
        if (!segmentRepository.existsByIdAndRouteId(segmentId, routeId)) {
            throw new PriceRecordNotFoundException(segmentId);
        }
        return priceRecordRepository.findBySegmentId(segmentId);
    }
}
