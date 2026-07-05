package com.flightmonitor.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateSegmentHttpRequest(
        @NotBlank(message = "Transport type is required")
        @Pattern(regexp = "^(FLIGHT|BUS|CAR|BOAT|OTHER)$", message = "Transport type must be FLIGHT, BUS, CAR, BOAT or OTHER")
        String transportType,

        String label
) {}
