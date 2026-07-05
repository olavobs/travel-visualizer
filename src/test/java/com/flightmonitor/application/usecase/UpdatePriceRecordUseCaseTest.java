package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.UpdatePriceRequest;
import com.flightmonitor.domain.exception.PriceRecordNotFoundException;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.domain.port.PriceCachePort;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdatePriceRecordUseCaseTest {

    @Mock private PriceRecordRepository priceRecordRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private SegmentRepository segmentRepository;
    @Mock private PriceCachePort priceCachePort;

    private UpdatePriceRecordUseCase useCase;

    private static final Long USER_ID    = 10L;
    private static final Long ROUTE_ID   = 1L;
    private static final Long SEG_ID     = 5L;
    private static final Long PRICE_ID   = 20L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new UpdatePriceRecordUseCase(priceRecordRepository, routeRepository, segmentRepository, priceCachePort);
    }

    @Test
    void shouldUpdateRecordAndEvictCache() {
        Money newMoney = new Money(new BigDecimal("2800.00"), Currency.USD);
        Instant newInstant = Instant.parse("2026-05-01T00:00:00Z");
        PriceRecord existing = new PriceRecord(PRICE_ID, SEG_ID, new Money(new BigDecimal("3200.00"), Currency.BRL), Instant.parse("2026-04-10T00:00:00Z"), false);
        UpdatePriceRequest request = new UpdatePriceRequest(USER_ID, ROUTE_ID, SEG_ID, PRICE_ID, newMoney, newInstant);

        when(routeRepository.existsByIdAndUserId(ROUTE_ID, USER_ID)).thenReturn(true);
        when(segmentRepository.existsByIdAndRouteId(SEG_ID, ROUTE_ID)).thenReturn(true);
        when(priceRecordRepository.findById(PRICE_ID)).thenReturn(Optional.of(existing));
        when(priceRecordRepository.update(any(PriceRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        PriceRecord result = useCase.execute(request);

        assertThat(result.getPrice().amount()).isEqualByComparingTo(new BigDecimal("2800.00"));
        assertThat(result.getPrice().currency()).isEqualTo(Currency.USD);
        assertThat(result.getRecordedAt()).isEqualTo(newInstant);
        assertThat(result.isPurchased()).isFalse();
        verify(priceCachePort, times(1)).evict(SEG_ID);
    }

    @Test
    void shouldPreservePurchasedFlagOnUpdate() {
        PriceRecord existing = new PriceRecord(PRICE_ID, SEG_ID, new Money(new BigDecimal("3200.00"), Currency.BRL), Instant.now(), true);
        UpdatePriceRequest request = new UpdatePriceRequest(USER_ID, ROUTE_ID, SEG_ID, PRICE_ID,
                new Money(new BigDecimal("2900.00"), Currency.BRL), Instant.now());

        when(routeRepository.existsByIdAndUserId(ROUTE_ID, USER_ID)).thenReturn(true);
        when(segmentRepository.existsByIdAndRouteId(SEG_ID, ROUTE_ID)).thenReturn(true);
        when(priceRecordRepository.findById(PRICE_ID)).thenReturn(Optional.of(existing));
        when(priceRecordRepository.update(any(PriceRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        PriceRecord result = useCase.execute(request);

        assertThat(result.isPurchased()).isTrue();
    }

    @Test
    void shouldThrowWhenRouteDoesNotBelongToUser() {
        when(routeRepository.existsByIdAndUserId(ROUTE_ID, USER_ID)).thenReturn(false);
        UpdatePriceRequest request = new UpdatePriceRequest(USER_ID, ROUTE_ID, SEG_ID, PRICE_ID,
                new Money(new BigDecimal("100"), Currency.BRL), Instant.now());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(RouteNotFoundException.class);

        verifyNoInteractions(priceRecordRepository, priceCachePort);
    }

    @Test
    void shouldThrowWhenPriceRecordNotFound() {
        when(routeRepository.existsByIdAndUserId(ROUTE_ID, USER_ID)).thenReturn(true);
        when(segmentRepository.existsByIdAndRouteId(SEG_ID, ROUTE_ID)).thenReturn(true);
        when(priceRecordRepository.findById(PRICE_ID)).thenReturn(Optional.empty());
        UpdatePriceRequest request = new UpdatePriceRequest(USER_ID, ROUTE_ID, SEG_ID, PRICE_ID,
                new Money(new BigDecimal("100"), Currency.BRL), Instant.now());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(PriceRecordNotFoundException.class);

        verify(priceRecordRepository, never()).update(any());
    }

    @Test
    void shouldThrowWhenPriceRecordBelongsToDifferentSegment() {
        PriceRecord existing = new PriceRecord(PRICE_ID, 999L, new Money(new BigDecimal("3200.00"), Currency.BRL), Instant.now());
        UpdatePriceRequest request = new UpdatePriceRequest(USER_ID, ROUTE_ID, SEG_ID, PRICE_ID,
                new Money(new BigDecimal("100"), Currency.BRL), Instant.now());

        when(routeRepository.existsByIdAndUserId(ROUTE_ID, USER_ID)).thenReturn(true);
        when(segmentRepository.existsByIdAndRouteId(SEG_ID, ROUTE_ID)).thenReturn(true);
        when(priceRecordRepository.findById(PRICE_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(PriceRecordNotFoundException.class);

        verify(priceRecordRepository, never()).update(any());
    }
}
