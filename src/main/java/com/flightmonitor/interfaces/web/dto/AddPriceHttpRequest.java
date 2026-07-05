package com.flightmonitor.interfaces.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddPriceHttpRequest(
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^(BRL|USD|EUR|GBP)$", message = "Currency must be BRL, USD, EUR or GBP")
        String currency,

        @NotNull(message = "Date seen is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate recordedDate
) {}
