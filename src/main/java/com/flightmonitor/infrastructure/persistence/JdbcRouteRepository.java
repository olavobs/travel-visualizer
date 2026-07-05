package com.flightmonitor.infrastructure.persistence;

import com.flightmonitor.domain.model.Route;
import com.flightmonitor.domain.model.RouteStatus;
import com.flightmonitor.domain.repository.RouteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRouteRepository implements RouteRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRouteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Route> ROW_MAPPER = (rs, rowNum) -> new Route(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("origin"),
            rs.getString("destination"),
            rs.getDate("travel_date").toLocalDate(),
            RouteStatus.valueOf(rs.getString("status"))
    );

    @Override
    public Route save(Route route) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO travel_routes (user_id, origin, destination, travel_date) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, route.getUserId());
            ps.setString(2, route.getOrigin());
            ps.setString(3, route.getDestination());
            ps.setDate(4, Date.valueOf(route.getTravelDate()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new Route(id, route.getUserId(), route.getOrigin(), route.getDestination(), route.getTravelDate(), RouteStatus.WATCHING);
    }

    @Override
    public List<Route> findAllByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT id, user_id, origin, destination, travel_date, status FROM travel_routes " +
                "WHERE user_id = ? ORDER BY travel_date ASC",
                ROW_MAPPER, userId
        );
    }

    @Override
    public Optional<Route> findById(Long id) {
        List<Route> results = jdbcTemplate.query(
                "SELECT id, user_id, origin, destination, travel_date, status FROM travel_routes WHERE id = ?",
                ROW_MAPPER, id
        );
        return results.stream().findFirst();
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM travel_routes WHERE id = ? AND user_id = ?",
                Integer.class, id, userId
        );
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM travel_routes WHERE id = ?", id);
    }

    @Override
    public Route updateStatus(Long routeId, Long userId, RouteStatus status) {
        jdbcTemplate.update(
                "UPDATE travel_routes SET status = ? WHERE id = ? AND user_id = ?",
                status.name(), routeId, userId
        );
        return findById(routeId).orElseThrow();
    }
}
