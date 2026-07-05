package com.flightmonitor.application.dto;

import com.flightmonitor.domain.model.Money;

import java.time.Instant;

public record AddPriceRequest(Long userId, Long routeId, Long segmentId, Money price, Instant recordedAt) {}
