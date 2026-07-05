package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.Segment;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListSegmentsUseCase {

    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;

    public ListSegmentsUseCase(RouteRepository routeRepository, SegmentRepository segmentRepository) {
        this.routeRepository   = routeRepository;
        this.segmentRepository = segmentRepository;
    }

    public List<Segment> execute(Long routeId, Long userId) {
        if (!routeRepository.existsByIdAndUserId(routeId, userId)) {
            throw new RouteNotFoundException(routeId);
        }
        return segmentRepository.findByRouteId(routeId);
    }
}
