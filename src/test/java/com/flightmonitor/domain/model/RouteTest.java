package com.flightmonitor.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.flightmonitor.domain.model.RouteStatus.WATCHING;

import static org.assertj.core.api.Assertions.*;

class RouteTest {

    private static final Long USER_ID = 1L;

    @Test
    void shouldCreateValidRoute() {
        Route route = new Route(USER_ID, "REC", "LIS", LocalDate.of(2026, 12, 1));

        assertThat(route.getOrigin()).isEqualTo("REC");
        assertThat(route.getDestination()).isEqualTo("LIS");
        assertThat(route.getTravelDate()).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(route.getId()).isNull();
        assertThat(route.getUserId()).isEqualTo(USER_ID);
        assertThat(route.getStatus()).isEqualTo(WATCHING);
    }

    @Test
    void shouldNormalizeCodesToUppercase() {
        Route route = new Route(USER_ID, "rec", "lis", LocalDate.of(2026, 12, 1));

        assertThat(route.getOrigin()).isEqualTo("REC");
        assertThat(route.getDestination()).isEqualTo("LIS");
    }

    @Test
    void shouldAcceptIdWhenReconstructingFromPersistence() {
        Route route = new Route(42L, USER_ID, "GRU", "CDG", LocalDate.of(2026, 6, 15));

        assertThat(route.getId()).isEqualTo(42L);
    }

    @Test
    void shouldRejectSameOriginAndDestination() {
        assertThatThrownBy(() -> new Route(USER_ID, "REC", "rec", LocalDate.of(2026, 12, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Origin and destination must be different");
    }

    @Test
    void shouldRejectBlankOrigin() {
        assertThatThrownBy(() -> new Route(USER_ID, "  ", "LIS", LocalDate.of(2026, 12, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Origin must not be blank");
    }

    @Test
    void shouldRejectBlankDestination() {
        assertThatThrownBy(() -> new Route(USER_ID, "REC", "", LocalDate.of(2026, 12, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Destination must not be blank");
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThatThrownBy(() -> new Route(USER_ID, null, "LIS", LocalDate.of(2026, 12, 1)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Origin must not be null");
    }

    @Test
    void shouldRejectNullTravelDate() {
        assertThatThrownBy(() -> new Route(USER_ID, "REC", "LIS", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Travel date must not be null");
    }
}
