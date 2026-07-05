package com.flightmonitor.infrastructure.persistence;

import com.flightmonitor.domain.model.*;
import com.flightmonitor.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class JdbcPriceRecordRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired private JdbcRouteRepository routeRepository;
    @Autowired private JdbcSegmentRepository segmentRepository;
    @Autowired private JdbcPriceRecordRepository priceRecordRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Long segmentId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO users (email, password) VALUES (?, ?)",
                "test-price-" + System.nanoTime() + "@example.com",
                "$2a$10$hashedpassword"
        );
        Long userId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Route route = routeRepository.save(new Route(userId, "REC", "LIS", LocalDate.of(2026, 12, 1)));
        Segment segment = segmentRepository.save(new Segment(route.getId(), TransportType.FLIGHT, null));
        segmentId = segment.getId();
    }

    @Test
    void shouldSavePriceRecordAndAssignGeneratedId() {
        Money money = new Money(new BigDecimal("3200.00"), Currency.BRL);
        PriceRecord record = new PriceRecord(segmentId, money, Instant.parse("2026-04-10T00:00:00Z"));

        PriceRecord saved = priceRecordRepository.save(record);

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getSegmentId()).isEqualTo(segmentId);
        assertThat(saved.getPrice().amount()).isEqualByComparingTo(new BigDecimal("3200.00"));
        assertThat(saved.getPrice().currency()).isEqualTo(Currency.BRL);
        assertThat(saved.isPurchased()).isFalse();
    }

    @Test
    void shouldFindAllRecordsBySegmentIdOrderedByDateDesc() {
        priceRecordRepository.save(new PriceRecord(segmentId, new Money(new BigDecimal("3200.00"), Currency.BRL), Instant.parse("2026-04-10T00:00:00Z")));
        priceRecordRepository.save(new PriceRecord(segmentId, new Money(new BigDecimal("2950.00"), Currency.USD), Instant.parse("2026-04-11T00:00:00Z")));

        List<PriceRecord> history = priceRecordRepository.findBySegmentId(segmentId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getRecordedAt()).isEqualTo(Instant.parse("2026-04-11T00:00:00Z"));
        assertThat(history.get(0).getPrice().currency()).isEqualTo(Currency.USD);
        assertThat(history.get(1).getRecordedAt()).isEqualTo(Instant.parse("2026-04-10T00:00:00Z"));
        assertThat(history.get(1).getPrice().currency()).isEqualTo(Currency.BRL);
    }

    @Test
    void shouldFindLowestPriceRecord() {
        priceRecordRepository.save(new PriceRecord(segmentId, new Money(new BigDecimal("3200.00"), Currency.BRL), Instant.parse("2026-04-10T00:00:00Z")));
        priceRecordRepository.save(new PriceRecord(segmentId, new Money(new BigDecimal("2950.00"), Currency.EUR), Instant.parse("2026-04-11T00:00:00Z")));
        priceRecordRepository.save(new PriceRecord(segmentId, new Money(new BigDecimal("3100.00"), Currency.BRL), Instant.parse("2026-04-12T00:00:00Z")));

        Optional<PriceRecord> lowest = priceRecordRepository.findLowestBySegmentId(segmentId);

        assertThat(lowest).isPresent();
        assertThat(lowest.get().getPrice().amount()).isEqualByComparingTo(new BigDecimal("2950.00"));
        assertThat(lowest.get().getPrice().currency()).isEqualTo(Currency.EUR);
    }

    @Test
    void shouldFindLatestPriceRecord() {
        priceRecordRepository.save(new PriceRecord(segmentId, new Money(new BigDecimal("3200.00"), Currency.BRL), Instant.parse("2026-04-10T00:00:00Z")));
        priceRecordRepository.save(new PriceRecord(segmentId, new Money(new BigDecimal("2950.00"), Currency.GBP), Instant.parse("2026-04-11T00:00:00Z")));

        Optional<PriceRecord> latest = priceRecordRepository.findLatestBySegmentId(segmentId);

        assertThat(latest).isPresent();
        assertThat(latest.get().getRecordedAt()).isEqualTo(Instant.parse("2026-04-11T00:00:00Z"));
        assertThat(latest.get().getPrice().currency()).isEqualTo(Currency.GBP);
    }

    @Test
    void shouldReturnEmptyWhenNoRecordsExist() {
        assertThat(priceRecordRepository.findLowestBySegmentId(segmentId)).isEmpty();
        assertThat(priceRecordRepository.findLatestBySegmentId(segmentId)).isEmpty();
        assertThat(priceRecordRepository.findBySegmentId(segmentId)).isEmpty();
    }
}
