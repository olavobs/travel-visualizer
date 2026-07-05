package com.flightmonitor.application.dto;

import com.flightmonitor.domain.model.Money;

import java.time.LocalDate;

public record UpdatePriceRequest(Long userId, Long routeId, Long segmentId, Long priceId, Money price, LocalDate recordedDate) {}
