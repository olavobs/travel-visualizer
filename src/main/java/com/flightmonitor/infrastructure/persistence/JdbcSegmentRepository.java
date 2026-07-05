package com.flightmonitor.infrastructure.persistence;

import com.flightmonitor.domain.model.Segment;
import com.flightmonitor.domain.model.TransportType;
import com.flightmonitor.domain.repository.SegmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcSegmentRepository implements SegmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSegmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Segment> ROW_MAPPER = (rs, rowNum) -> new Segment(
            rs.getLong("id"),
            rs.getLong("route_id"),
            TransportType.valueOf(rs.getString("transport_type")),
            rs.getString("label")
    );

    @Override
    public Segment save(Segment segment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO travel_segments (route_id, transport_type, label) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, segment.getRouteId());
            ps.setString(2, segment.getTransportType().name());
            ps.setString(3, segment.getLabel());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new Segment(id, segment.getRouteId(), segment.getTransportType(), segment.getLabel());
    }

    @Override
    public List<Segment> findByRouteId(Long routeId) {
        return jdbcTemplate.query(
                "SELECT id, route_id, transport_type, label FROM travel_segments WHERE route_id = ? ORDER BY id ASC",
                ROW_MAPPER, routeId
        );
    }

    @Override
    public Optional<Segment> findById(Long id) {
        List<Segment> results = jdbcTemplate.query(
                "SELECT id, route_id, transport_type, label FROM travel_segments WHERE id = ?",
                ROW_MAPPER, id
        );
        return results.stream().findFirst();
    }

    @Override
    public boolean existsByIdAndRouteId(Long id, Long routeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM travel_segments WHERE id = ? AND route_id = ?",
                Integer.class, id, routeId
        );
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM travel_segments WHERE id = ?", id);
    }
}
