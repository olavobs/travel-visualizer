package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRoutesUseCase {

    private final RouteRepository routeRepository;

    public ListRoutesUseCase(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> execute(Long userId) {
        return routeRepository.findAllByUserId(userId);
    }
}
