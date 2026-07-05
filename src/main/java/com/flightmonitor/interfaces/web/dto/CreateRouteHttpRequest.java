package com.flightmonitor.interfaces.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateRouteHttpRequest(
        @NotBlank(message = "Origin is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Origin must be a 3-letter IATA code")
        String origin,

        @NotBlank(message = "Destination is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Destination must be a 3-letter IATA code")
        String destination,

        @NotNull(message = "Travel date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate travelDate
) {}
