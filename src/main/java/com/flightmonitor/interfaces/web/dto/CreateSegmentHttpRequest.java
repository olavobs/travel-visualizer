package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.domain.model.TransportType;
import jakarta.validation.constraints.NotNull;

public record CreateSegmentHttpRequest(
        @NotNull(message = "Transport type is required")
        TransportType transportType,

        String label
) {
}
