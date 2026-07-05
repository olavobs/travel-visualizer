package com.flightmonitor.interfaces.web.dto;

import com.flightmonitor.domain.model.RouteStatus;
import jakarta.validation.constraints.NotNull;

public record SetStatusRequest(
        @NotNull(message = "Status is required")
        RouteStatus status
) {
}
