package com.flightmonitor.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class PriceRecordTest {

    @Test
    void shouldCreateValidPriceRecord() {
        Money money = new Money(new BigDecimal("3200.00"), Currency.BRL);
        Instant recordedAt = Instant.parse("2026-04-10T00:00:00Z");
        PriceRecord record = new PriceRecord(1L, money, recordedAt);

        assertThat(record.getSegmentId()).isEqualTo(1L);
        assertThat(record.getPrice().amount()).isEqualByComparingTo(new BigDecimal("3200.00"));
        assertThat(record.getPrice().currency()).isEqualTo(Currency.BRL);
        assertThat(record.getRecordedAt()).isEqualTo(recordedAt);
        assertThat(record.getId()).isNull();
        assertThat(record.isPurchased()).isFalse();
    }

    @Test
    void shouldRejectZeroPrice() {
        assertThatThrownBy(() -> new PriceRecord(1L, new Money(BigDecimal.ZERO, Currency.BRL), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() -> new PriceRecord(1L, new Money(new BigDecimal("-0.01"), Currency.BRL), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");
    }

    @Test
    void shouldAcceptAllSupportedCurrencies() {
        for (Currency currency : Currency.values()) {
            assertThatCode(() -> new PriceRecord(1L, new Money(new BigDecimal("100"), currency), Instant.now()))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void shouldRejectNullSegmentId() {
        assertThatThrownBy(() -> new PriceRecord(null, new Money(new BigDecimal("100"), Currency.BRL), Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Segment ID must not be null");
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatThrownBy(() -> new PriceRecord(1L, null, Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Price must not be null");
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThatThrownBy(() -> new Money(new BigDecimal("100"), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Currency must not be null");
    }

    @Test
    void shouldRejectNullRecordedAt() {
        assertThatThrownBy(() -> new PriceRecord(1L, new Money(new BigDecimal("100"), Currency.BRL), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Recorded instant must not be null");
    }
}
