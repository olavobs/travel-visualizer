package com.flightmonitor.domain.repository;

import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.model.RouteStatus;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {

    Route save(Route route);

    List<Route> findAllByUserId(Long userId);

    Optional<Route> findById(Long id);

    boolean existsByIdAndUserId(Long id, Long userId);

    void deleteById(Long id);

    Route updateStatus(Long routeId, Long userId, RouteStatus status);

    List<Route> findAllByStatus(RouteStatus status);
}
