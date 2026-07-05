package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.domain.model.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdatePriceHttpRequest(
        @NotNull(message = "Money is required")
        @Valid
        Money money,

        @NotNull(message = "Date seen is required")
        Instant recordedAt
) {
}
