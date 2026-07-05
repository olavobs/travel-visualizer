package com.flightmonitor.infrastructure.cache;

import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class RedisPriceCacheAdapterIntegrationTest extends AbstractIntegrationTest {

    private static final Long ROUTE_ID = 1L;

    @Autowired
    private RedisPriceCacheAdapter cacheAdapter;

    @AfterEach
    void cleanup() {
        cacheAdapter.evict(ROUTE_ID);
    }

    @Test
    void shouldStoreAndRetrieveLatestPrice() {
        var money = new Money(new BigDecimal("3200.00"), Currency.BRL);

        cacheAdapter.storeLatestPrice(ROUTE_ID, money);

        Optional<Money> retrieved = cacheAdapter.getLatestPrice(ROUTE_ID);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().amount()).isEqualByComparingTo(new BigDecimal("3200.00"));
        assertThat(retrieved.get().currency()).isEqualTo(Currency.BRL);
    }

    @Test
    void shouldReturnEmptyOnCacheMiss() {
        assertThat(cacheAdapter.getLatestPrice(ROUTE_ID)).isEmpty();
    }

    @Test
    void shouldEvictPriceEntry() {
        cacheAdapter.storeLatestPrice(ROUTE_ID, new Money(new BigDecimal("2500.00"), Currency.EUR));

        cacheAdapter.evict(ROUTE_ID);

        assertThat(cacheAdapter.getLatestPrice(ROUTE_ID)).isEmpty();
    }

    @Test
    void shouldOverwriteExistingCachedPrice() {
        cacheAdapter.storeLatestPrice(ROUTE_ID, new Money(new BigDecimal("3200.00"), Currency.USD));
        cacheAdapter.storeLatestPrice(ROUTE_ID, new Money(new BigDecimal("2800.00"), Currency.GBP));

        Optional<Money> retrieved = cacheAdapter.getLatestPrice(ROUTE_ID);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().amount()).isEqualByComparingTo(new BigDecimal("2800.00"));
        assertThat(retrieved.get().currency()).isEqualTo(Currency.GBP);
    }

    @Test
    void shouldUseCorrectKeyFormat() {
        assertThat(cacheAdapter.buildKey(42L)).isEqualTo("route:price:latest:42");
    }
}
