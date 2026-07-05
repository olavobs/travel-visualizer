package com.flightmonitor.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class PriceRecordTest {

    @Test
    void shouldCreateValidPriceRecord() {
        var money = new Money(new BigDecimal("3200.00"), Currency.BRL);
        PriceRecord record = new PriceRecord(1L, money, LocalDate.of(2026, 4, 10));

        assertThat(record.getSegmentId()).isEqualTo(1L);
        assertThat(record.getPrice().amount()).isEqualByComparingTo(new BigDecimal("3200.00"));
        assertThat(record.getPrice().currency()).isEqualTo(Currency.BRL);
        assertThat(record.getRecordedDate()).isEqualTo(LocalDate.of(2026, 4, 10));
        assertThat(record.getId()).isNull();
        assertThat(record.isPurchased()).isFalse();
    }

    @Test
    void shouldRejectZeroPrice() {
        assertThatThrownBy(() -> new PriceRecord(1L, new Money(BigDecimal.ZERO, Currency.BRL), LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() -> new PriceRecord(1L, new Money(new BigDecimal("-0.01"), Currency.BRL), LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");
    }

    @Test
    void shouldAcceptAllSupportedCurrencies() {
        for (Currency currency : Currency.values()) {
            assertThatCode(() -> new PriceRecord(1L, new Money(new BigDecimal("100"), currency), LocalDate.now()))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void shouldRejectNullSegmentId() {
        assertThatThrownBy(() -> new PriceRecord(null, new Money(new BigDecimal("100"), Currency.BRL), LocalDate.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Segment ID must not be null");
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatThrownBy(() -> new PriceRecord(1L, null, LocalDate.now()))
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
    void shouldRejectNullRecordedDate() {
        assertThatThrownBy(() -> new PriceRecord(1L, new Money(new BigDecimal("100"), Currency.BRL), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Recorded date must not be null");
    }
}
