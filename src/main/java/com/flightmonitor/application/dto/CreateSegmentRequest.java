package com.flightmonitor.application.dto;

import com.flightmonitor.domain.model.TransportType;

public record CreateSegmentRequest(Long userId, Long routeId, TransportType transportType, String label) {}
