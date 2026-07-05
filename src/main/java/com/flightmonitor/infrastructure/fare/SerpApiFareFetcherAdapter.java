package com.flightmonitor.infrastructure.fare;

import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.port.FareFetcherPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnExpression("'${serpapi.api-key:}' != ''")
public class SerpApiFareFetcherAdapter implements FareFetcherPort {

    private final SerpApiClient client;

    public SerpApiFareFetcherAdapter(SerpApiClient client) {
        this.client = client;
    }

    @Override
    public Optional<Money> fetchLowestFare(Route route) {
        return client
                .fetchLowestPrice(route.getOrigin(), route.getDestination(), route.getTravelDate())
                .map(amount -> new Money(amount, Currency.BRL));
    }
}
