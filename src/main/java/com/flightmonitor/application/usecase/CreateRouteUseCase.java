package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.CreateRouteRequest;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.repository.RouteRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateRouteUseCase {

    private final RouteRepository routeRepository;

    public CreateRouteUseCase(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route execute(CreateRouteRequest request) {
        Route route = new Route(
                request.userId(),
                request.origin(),
                request.destination(),
                request.travelDate()
        );
        return routeRepository.save(route);
    }
}
