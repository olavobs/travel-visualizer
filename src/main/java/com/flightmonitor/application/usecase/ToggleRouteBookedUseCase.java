package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.model.RouteStatus;
import com.flightmonitor.domain.repository.RouteRepository;
import org.springframework.stereotype.Service;

@Service
public class ToggleRouteBookedUseCase {

    private final RouteRepository routeRepository;

    public ToggleRouteBookedUseCase(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route execute(Long routeId, Long userId, RouteStatus status) {
        if (!routeRepository.existsByIdAndUserId(routeId, userId)) {
            throw new RouteNotFoundException(routeId);
        }
        return routeRepository.updateStatus(routeId, userId, status);
    }
}
