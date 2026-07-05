package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.domain.model.Route;

import java.time.LocalDate;

public record RouteResponse(Long id, String origin, String destination, LocalDate travelDate, String status) {

    public static RouteResponse from(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getOrigin(),
                route.getDestination(),
                route.getTravelDate(),
                route.getStatus().name()
        );
    }
}
