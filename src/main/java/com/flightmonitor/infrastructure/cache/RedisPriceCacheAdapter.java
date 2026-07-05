package com.flightmonitor.infrastructure.cache;

import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.port.PriceCachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/**
 * Redis implementation of PriceCachePort.
 *
 * Key pattern: "route:price:latest:{routeId}"
 * Value format: "{amount}:{currency}" e.g. "3200.00:BRL"
 *
 * Storing amount and currency together in one key keeps them
 * atomically consistent — no risk of reading a stale currency
 * with an updated price.
 */
@Component
public class RedisPriceCacheAdapter implements PriceCachePort {

    static final String KEY_PREFIX = "route:price:latest:";
    private static final String SEPARATOR = ":";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public RedisPriceCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void storeLatestPrice(Long routeId, Money price) {
        String value = price.amount().toPlainString() + SEPARATOR + price.currency().name();
        redisTemplate.opsForValue().set(buildKey(routeId), value, TTL);
    }

    @Override
    public Optional<Money> getLatestPrice(Long routeId) {
        String value = redisTemplate.opsForValue().get(buildKey(routeId));
        if (value == null) return Optional.empty();
        String[] parts = value.split(SEPARATOR, 2);
        return Optional.of(new Money(new BigDecimal(parts[0]), Currency.valueOf(parts[1])));
    }

    @Override
    public void evict(Long routeId) {
        redisTemplate.delete(buildKey(routeId));
    }

    String buildKey(Long routeId) { return KEY_PREFIX + routeId; }
}
