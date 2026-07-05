package com.flightmonitor.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(WATCHING|BOOKED|CANCELLED)$", message = "Status must be WATCHING, BOOKED or CANCELLED")
        String status
) {}
