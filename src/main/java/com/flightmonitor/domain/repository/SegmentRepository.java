package com.flightmonitor.domain.repository;

import com.flightmonitor.domain.model.Segment;

import java.util.List;
import java.util.Optional;

public interface SegmentRepository {

    Segment save(Segment segment);

    List<Segment> findByRouteId(Long routeId);

    Optional<Segment> findById(Long id);

    boolean existsByIdAndRouteId(Long id, Long routeId);

    void deleteById(Long id);
}
