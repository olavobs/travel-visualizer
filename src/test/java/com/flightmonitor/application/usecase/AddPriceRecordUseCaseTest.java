package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.AddPriceRequest;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddPriceRecordUseCaseTest {

    @Mock private PriceRecordRepository priceRecordRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private SegmentRepository segmentRepository;
    @Mock private PriceCachePort priceCachePort;

    private AddPriceRecordUseCase useCase;

    private static final Long USER_ID   = 10L;
    private static final Long ROUTE_ID  = 1L;
    private static final Long SEG_ID    = 5L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new AddPriceRecordUseCase(priceRecordRepository, routeRepository, segmentRepository, priceCachePort);
    }

    @Test
    void shouldSaveRecordAndEvictCache() {
        Money money = new Money(new BigDecimal("2950.00"), Currency.BRL);
        Instant recordedAt = Instant.parse("2026-04-11T00:00:00Z");
        AddPriceRequest request = new AddPriceRequest(USER_ID, ROUTE_ID, SEG_ID, money, recordedAt);
        PriceRecord savedRecord = new PriceRecord(1L, SEG_ID, money, recordedAt);

        when(routeRepository.existsByIdAndUserId(ROUTE_ID, USER_ID)).thenReturn(true);
        when(segmentRepository.existsByIdAndRouteId(SEG_ID, ROUTE_ID)).thenReturn(true);
        when(priceRecordRepository.save(any(PriceRecord.class))).thenReturn(savedRecord);

        PriceRecord result = useCase.execute(request);

        assertThat(result.getPrice().amount()).isEqualByComparingTo(new BigDecimal("2950.00"));
        assertThat(result.getPrice().currency()).isEqualTo(Currency.BRL);
        assertThat(result.getRecordedAt()).isEqualTo(recordedAt);
        verify(priceCachePort, times(1)).evict(SEG_ID);
        verify(priceRecordRepository, times(1)).save(any(PriceRecord.class));
    }

    @Test
    void shouldThrowRouteNotFoundWhenRouteDoesNotBelongToUser() {
        when(routeRepository.existsByIdAndUserId(99L, USER_ID)).thenReturn(false);
        AddPriceRequest request = new AddPriceRequest(USER_ID, 99L, SEG_ID,
                new Money(new BigDecimal("100"), Currency.USD), Instant.now());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("99");

        verifyNoInteractions(priceRecordRepository, priceCachePort);
    }
}
