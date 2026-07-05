package com.flightmonitor.application.dto;

import java.time.LocalDate;

public record CreateRouteRequest(Long userId, String origin, String destination, LocalDate travelDate) {}
