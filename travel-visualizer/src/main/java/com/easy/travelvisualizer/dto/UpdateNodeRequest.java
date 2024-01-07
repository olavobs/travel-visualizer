package com.easy.travelvisualizer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateNodeRequest(String currentCity,
                                BigDecimal price,
                                Instant startMoment,
                                Instant endMoment,
                                String currency,
                                String transportCompanyName,
                                String departurePlace,
                                String arrivalPlace,
                                String transportType,
                                Long currentNodeId) {
}
