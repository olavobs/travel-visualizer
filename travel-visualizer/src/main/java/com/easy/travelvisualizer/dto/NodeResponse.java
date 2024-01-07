package com.easy.travelvisualizer.dto;

import java.math.BigDecimal;

public record NodeResponse(String currentCity,
                           BigDecimal price,
                           String startMoment,
                           String endMoment,
                           String currency,
                           String transportCompanyName,
                           String departurePlace,
                           String arrivalPlace,
                           String transportType,
                           String previousCity,
                           BigDecimal pricePath,
                           Long id) {
}
