package com.flightmonitor.infrastructure.persistence;

import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.model.RouteStatus;
import com.flightmonitor.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class JdbcRouteRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcRouteRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    void insertTestUser() {
        jdbcTemplate.update(
                "INSERT INTO users (email, password) VALUES (?, ?)",
                "test-route-" + System.nanoTime() + "@example.com",
                "$2a$10$hashedpassword"
        );
        userId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    @Test
    void shouldSaveRouteAndAssignGeneratedId() {
        Route route = new Route(userId, "GRU", "CDG", LocalDate.of(2026, 12, 15));

        Route saved = repository.save(route);

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getOrigin()).isEqualTo("GRU");
        assertThat(saved.getDestination()).isEqualTo("CDG");
        assertThat(saved.getTravelDate()).isEqualTo(LocalDate.of(2026, 12, 15));
        assertThat(saved.getUserId()).isEqualTo(userId);
    }

    @Test
    void shouldFindSavedRouteById() {
        Route saved = repository.save(new Route(userId, "FOR", "FRA", LocalDate.of(2026, 10, 1)));

        Optional<Route> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrigin()).isEqualTo("FOR");
        assertThat(found.get().getDestination()).isEqualTo("FRA");
    }

    @Test
    void shouldReturnEmptyForNonExistingId() {
        Optional<Route> result = repository.findById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllSavedRoutesByUserId() {
        repository.save(new Route(userId, "GRU", "LHR", LocalDate.of(2026, 11, 1)));
        repository.save(new Route(userId, "BSB", "MAD", LocalDate.of(2026, 11, 2)));

        List<Route> routes = repository.findAllByUserId(userId);

        assertThat(routes).hasSizeGreaterThanOrEqualTo(2);
        assertThat(routes).allMatch(r -> r.getUserId().equals(userId));
    }

    @Test
    void shouldReturnTrueForExistingRouteAndUser() {
        Route saved = repository.save(new Route(userId, "SSA", "LIS", LocalDate.of(2026, 9, 1)));

        assertThat(repository.existsByIdAndUserId(saved.getId(), userId)).isTrue();
    }

    @Test
    void shouldReturnFalseForRouteWithWrongUser() {
        Route saved = repository.save(new Route(userId, "REC", "GIG", LocalDate.of(2026, 9, 1)));

        assertThat(repository.existsByIdAndUserId(saved.getId(), 999L)).isFalse();
    }

    @Test
    void shouldReturnFalseForNonExistingRoute() {
        assertThat(repository.existsByIdAndUserId(Long.MAX_VALUE, userId)).isFalse();
    }

    @Test
    void shouldDefaultStatusToWatchingOnSave() {
        Route saved = repository.save(new Route(userId, "GRU", "JFK", LocalDate.of(2026, 8, 1)));

        assertThat(saved.getStatus()).isEqualTo(RouteStatus.WATCHING);
    }

    @Test
    void shouldUpdateStatus() {
        Route saved = repository.save(new Route(userId, "GRU", "NRT", LocalDate.of(2026, 9, 1)));

        Route booked = repository.updateStatus(saved.getId(), userId, RouteStatus.BOOKED);
        assertThat(booked.getStatus()).isEqualTo(RouteStatus.BOOKED);

        Route cancelled = repository.updateStatus(saved.getId(), userId, RouteStatus.CANCELLED);
        assertThat(cancelled.getStatus()).isEqualTo(RouteStatus.CANCELLED);

        Route watching = repository.updateStatus(saved.getId(), userId, RouteStatus.WATCHING);
        assertThat(watching.getStatus()).isEqualTo(RouteStatus.WATCHING);
    }
}
