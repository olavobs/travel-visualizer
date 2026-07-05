package com.flightmonitor.domain.port;

import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.Route;

import java.util.Optional;

public interface FareFetcherPort {

    /**
     * Fetch the lowest available fare for the given route.
     * Returns empty if no offers are found or the external API fails.
     */
    Optional<Money> fetchLowestFare(Route route);
}
