package com.flightmonitor.infrastructure.fare;

import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.port.FareFetcherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

public class StubFareFetcherAdapter implements FareFetcherPort {

    private static final Logger log = LoggerFactory.getLogger(StubFareFetcherAdapter.class);
    private final Random random = new Random();

    @Override
    public Optional<Money> fetchLowestFare(Route route) {
        BigDecimal price = BigDecimal.valueOf(500 + random.nextInt(4500));
        log.info("[STUB] Fake fare R${} for {}->{}", price, route.getOrigin(), route.getDestination());
        return Optional.of(new Money(price, Currency.BRL));
    }
}
