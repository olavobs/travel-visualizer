package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.CreateSegmentRequest;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.Segment;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateSegmentUseCase {

    private final RouteRepository routeRepository;
    private final SegmentRepository segmentRepository;

    public CreateSegmentUseCase(RouteRepository routeRepository, SegmentRepository segmentRepository) {
        this.routeRepository   = routeRepository;
        this.segmentRepository = segmentRepository;
    }

    public Segment execute(CreateSegmentRequest request) {
        if (!routeRepository.existsByIdAndUserId(request.routeId(), request.userId())) {
            throw new RouteNotFoundException(request.routeId());
        }
        return segmentRepository.save(new Segment(request.routeId(), request.transportType(), request.label()));
    }
}
