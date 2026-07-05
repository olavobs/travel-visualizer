package com.flightmonitor.application.usecase;

import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.model.RouteStatus;
import com.flightmonitor.domain.model.Segment;
import com.flightmonitor.domain.model.TransportType;
import com.flightmonitor.domain.port.FareFetcherPort;
import com.flightmonitor.domain.port.PriceCachePort;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import com.flightmonitor.domain.repository.RouteRepository;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FetchAndStoreDailyPricesUseCaseTest {

    @Mock private RouteRepository routeRepository;
    @Mock private SegmentRepository segmentRepository;
    @Mock private PriceRecordRepository priceRecordRepository;
    @Mock private FareFetcherPort fareFetcherPort;
    @Mock private PriceCachePort priceCachePort;

    private FetchAndStoreDailyPricesUseCase useCase;

    private final Route watchingRoute = new Route(1L, 10L, "GRU", "LIS", LocalDate.of(2026, 12, 1), RouteStatus.WATCHING);
    private final Segment flightSegment = new Segment(5L, 1L, TransportType.FLIGHT, null);
    private final Segment busSegment    = new Segment(6L, 1L, TransportType.BUS, null);
    private final Money fare = new Money(new BigDecimal("3200.00"), Currency.BRL);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new FetchAndStoreDailyPricesUseCase(routeRepository, segmentRepository, priceRecordRepository, fareFetcherPort, priceCachePort);
    }

    @Test
    void shouldSavePriceAndEvictCacheForFlightSegment() {
        when(routeRepository.findAllByStatus(RouteStatus.WATCHING)).thenReturn(List.of(watchingRoute));
        when(segmentRepository.findByRouteId(1L)).thenReturn(List.of(flightSegment));
        when(fareFetcherPort.fetchLowestFare(watchingRoute)).thenReturn(Optional.of(fare));

        useCase.execute();

        verify(priceRecordRepository, times(1)).save(any());
        verify(priceCachePort, times(1)).evict(5L);
    }

    @Test
    void shouldSkipNonFlightSegments() {
        when(routeRepository.findAllByStatus(RouteStatus.WATCHING)).thenReturn(List.of(watchingRoute));
        when(segmentRepository.findByRouteId(1L)).thenReturn(List.of(busSegment));

        useCase.execute();

        verifyNoInteractions(fareFetcherPort, priceRecordRepository, priceCachePort);
    }

    @Test
    void shouldSkipSaveWhenFarePortReturnsEmpty() {
        when(routeRepository.findAllByStatus(RouteStatus.WATCHING)).thenReturn(List.of(watchingRoute));
        when(segmentRepository.findByRouteId(1L)).thenReturn(List.of(flightSegment));
        when(fareFetcherPort.fetchLowestFare(watchingRoute)).thenReturn(Optional.empty());

        useCase.execute();

        verifyNoInteractions(priceRecordRepository, priceCachePort);
    }

    @Test
    void shouldContinueProcessingOtherSegmentsWhenOneFails() {
        Segment flightSegment2 = new Segment(7L, 1L, TransportType.FLIGHT, null);

        when(routeRepository.findAllByStatus(RouteStatus.WATCHING)).thenReturn(List.of(watchingRoute));
        when(segmentRepository.findByRouteId(1L)).thenReturn(List.of(flightSegment, flightSegment2));
        when(fareFetcherPort.fetchLowestFare(watchingRoute))
                .thenThrow(new RuntimeException("API error"))
                .thenReturn(Optional.of(fare));

        useCase.execute();

        verify(priceRecordRepository, times(1)).save(any());
        verify(priceCachePort, times(1)).evict(7L);
    }

    @Test
    void shouldDoNothingWhenNoWatchingRoutesExist() {
        when(routeRepository.findAllByStatus(RouteStatus.WATCHING)).thenReturn(List.of());

        useCase.execute();

        verifyNoInteractions(segmentRepository, fareFetcherPort, priceRecordRepository, priceCachePort);
    }

    @Test
    void shouldProcessMixedSegmentsCorrectly() {
        when(routeRepository.findAllByStatus(RouteStatus.WATCHING)).thenReturn(List.of(watchingRoute));
        when(segmentRepository.findByRouteId(1L)).thenReturn(List.of(flightSegment, busSegment));
        when(fareFetcherPort.fetchLowestFare(watchingRoute)).thenReturn(Optional.of(fare));

        useCase.execute();

        verify(fareFetcherPort, times(1)).fetchLowestFare(watchingRoute);
        verify(priceRecordRepository, times(1)).save(any());
        verify(priceCachePort, times(1)).evict(5L);
        verify(priceCachePort, never()).evict(6L);
    }
}
