package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.RouteSummaryResponse;
import com.flightmonitor.domain.exception.RouteNotFoundException;
import com.flightmonitor.domain.model.*;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetRoutePriceSummaryUseCaseTest {

    @Mock private RouteRepository routeRepository;
    @Mock private SegmentRepository segmentRepository;
    @Mock private PriceRecordRepository priceRecordRepository;
    @Mock private PriceCachePort priceCachePort;

    private GetRoutePriceSummaryUseCase useCase;

    private static final Long USER_ID  = 10L;
    private static final Long ROUTE_ID = 1L;
    private static final Long SEG_ID   = 5L;

    private final Route route = new Route(ROUTE_ID, USER_ID, "REC", "LIS", LocalDate.of(2026, 12, 1));
    private final Segment segment = new Segment(SEG_ID, ROUTE_ID, TransportType.FLIGHT, null);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new GetRoutePriceSummaryUseCase(routeRepository, segmentRepository, priceRecordRepository, priceCachePort);
    }

    @Test
    void shouldReturnLatestPriceFromCacheOnCacheHit() {
        var cachedMoney = new Money(new BigDecimal("3200.00"), Currency.BRL);
        var lowestMoney = new Money(new BigDecimal("2950.00"), Currency.USD);

        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route));
        when(segmentRepository.findByRouteId(ROUTE_ID)).thenReturn(List.of(segment));
        when(priceCachePort.getLatestPrice(SEG_ID)).thenReturn(Optional.of(cachedMoney));
        when(priceRecordRepository.findLowestBySegmentId(SEG_ID))
                .thenReturn(Optional.of(new PriceRecord(2L, SEG_ID, lowestMoney, Instant.parse("2026-04-11T00:00:00Z"))));
        when(priceRecordRepository.findPurchasedBySegmentId(SEG_ID)).thenReturn(Optional.empty());

        RouteSummaryResponse result = useCase.execute(ROUTE_ID, USER_ID);

        assertThat(result.segments()).hasSize(1);
        var seg = result.segments().get(0);
        assertThat(seg.latestPrice().amount()).isEqualByComparingTo(new BigDecimal("3200.00"));
        assertThat(seg.latestPrice().currency()).isEqualTo(Currency.BRL);
        assertThat(seg.lowestPrice().amount()).isEqualByComparingTo(new BigDecimal("2950.00"));
        verify(priceRecordRepository, never()).findLatestBySegmentId(any());
        verify(priceCachePort, never()).storeLatestPrice(any(), any());
    }

    @Test
    void shouldFallbackToDbAndPopulateCacheOnCacheMiss() {
        var dbMoney = new Money(new BigDecimal("3200.00"), Currency.EUR);
        PriceRecord latest = new PriceRecord(1L, SEG_ID, dbMoney, Instant.parse("2026-04-10T00:00:00Z"));

        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route));
        when(segmentRepository.findByRouteId(ROUTE_ID)).thenReturn(List.of(segment));
        when(priceCachePort.getLatestPrice(SEG_ID)).thenReturn(Optional.empty());
        when(priceRecordRepository.findLatestBySegmentId(SEG_ID)).thenReturn(Optional.of(latest));
        when(priceRecordRepository.findLowestBySegmentId(SEG_ID)).thenReturn(Optional.of(latest));
        when(priceRecordRepository.findPurchasedBySegmentId(SEG_ID)).thenReturn(Optional.empty());

        RouteSummaryResponse result = useCase.execute(ROUTE_ID, USER_ID);

        assertThat(result.segments().get(0).latestPrice().currency()).isEqualTo(Currency.EUR);
        verify(priceCachePort, times(1)).storeLatestPrice(SEG_ID, dbMoney);
    }

    @Test
    void shouldReturnNullPricesWhenNoRecordsExist() {
        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route));
        when(segmentRepository.findByRouteId(ROUTE_ID)).thenReturn(List.of(segment));
        when(priceCachePort.getLatestPrice(SEG_ID)).thenReturn(Optional.empty());
        when(priceRecordRepository.findLatestBySegmentId(SEG_ID)).thenReturn(Optional.empty());
        when(priceRecordRepository.findLowestBySegmentId(SEG_ID)).thenReturn(Optional.empty());
        when(priceRecordRepository.findPurchasedBySegmentId(SEG_ID)).thenReturn(Optional.empty());

        RouteSummaryResponse result = useCase.execute(ROUTE_ID, USER_ID);

        var seg = result.segments().get(0);
        assertThat(seg.latestPrice()).isNull();
        assertThat(seg.lowestPrice()).isNull();
        assertThat(seg.purchasedPrice()).isNull();
        verify(priceCachePort, never()).storeLatestPrice(any(), any());
    }

    @Test
    void shouldThrowWhenRouteNotFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, USER_ID))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldThrowWhenRouteBelongsToDifferentUser() {
        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> useCase.execute(ROUTE_ID, 999L))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining(String.valueOf(ROUTE_ID));
    }
}
