package com.flightmonitor.domain.exception;

public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(Long routeId) {
        super("Flight route not found with id: " + routeId);
    }
}
