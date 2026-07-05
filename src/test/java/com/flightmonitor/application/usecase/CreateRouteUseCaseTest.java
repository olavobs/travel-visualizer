package com.flightmonitor.application.usecase;

import com.flightmonitor.application.dto.CreateRouteRequest;
import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateRouteUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    private CreateRouteUseCase useCase;

    private static final Long USER_ID = 10L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new CreateRouteUseCase(routeRepository);
    }

    @Test
    void shouldDelegateToRepositoryAndReturnSavedRoute() {
        CreateRouteRequest request = new CreateRouteRequest(USER_ID, "REC", "LIS", LocalDate.of(2026, 12, 1));
        Route persisted = new Route(1L, USER_ID, "REC", "LIS", LocalDate.of(2026, 12, 1));

        when(routeRepository.save(any(Route.class))).thenReturn(persisted);

        Route result = useCase.execute(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrigin()).isEqualTo("REC");
        assertThat(result.getDestination()).isEqualTo("LIS");
        assertThat(result.getStatus().name()).isEqualTo("WATCHING");
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    void shouldPassCorrectValuesToDomainObject() {
        LocalDate travelDate = LocalDate.of(2026, 12, 1);
        CreateRouteRequest request = new CreateRouteRequest(USER_ID, "gru", "cdg", travelDate);
        Route persisted = new Route(2L, USER_ID, "GRU", "CDG", travelDate);

        when(routeRepository.save(any(Route.class))).thenReturn(persisted);

        Route result = useCase.execute(request);

        assertThat(result.getOrigin()).isEqualTo("GRU");
        assertThat(result.getDestination()).isEqualTo("CDG");
        assertThat(result.getTravelDate()).isEqualTo(travelDate);
    }
}
