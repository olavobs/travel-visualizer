package com.flightmonitor.application.dto;

import com.flightmonitor.domain.model.Money;

import java.time.LocalDate;

public record AddPriceRequest(Long userId, Long routeId, Long segmentId, Money price, LocalDate recordedDate) {}
