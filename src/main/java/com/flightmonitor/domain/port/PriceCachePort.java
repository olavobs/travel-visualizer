package com.flightmonitor.domain.port;

import com.flightmonitor.domain.model.Money;

import java.util.Optional;

/**
 * Port for price caching. Defined in the domain layer so the application
 * does not depend on a specific cache technology.
 *
 * Caches the full Money value (amount + currency) as a unit so that
 * price and currency are always kept in sync.
 */
public interface PriceCachePort {

    void storeLatestPrice(Long routeId, Money price);
    Optional<Money> getLatestPrice(Long routeId);
    void evict(Long routeId);
}
