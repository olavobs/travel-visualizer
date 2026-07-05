package com.flightmonitor.infrastructure.persistence;

import com.flightmonitor.domain.model.Currency;
import com.flightmonitor.domain.model.Money;
import com.flightmonitor.domain.model.PriceRecord;
import com.flightmonitor.domain.repository.PriceRecordRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPriceRecordRepository implements PriceRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPriceRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<PriceRecord> ROW_MAPPER = (rs, rowNum) -> new PriceRecord(
            rs.getLong("id"),
            rs.getLong("segment_id"),
            new Money(rs.getBigDecimal("price"), Currency.valueOf(rs.getString("currency"))),
            rs.getTimestamp("recorded_at").toInstant(),
            rs.getBoolean("purchased")
    );

    @Override
    public PriceRecord save(PriceRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO price_records (segment_id, price, currency, recorded_at, purchased) VALUES (?, ?, ?, ?, FALSE)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, record.getSegmentId());
            ps.setBigDecimal(2, record.getPrice().amount());
            ps.setString(3, record.getPrice().currency().name());
            ps.setTimestamp(4, Timestamp.from(record.getRecordedAt()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new PriceRecord(id, record.getSegmentId(), record.getPrice(), record.getRecordedAt(), false);
    }

    @Override
    public List<PriceRecord> findBySegmentId(Long segmentId) {
        return jdbcTemplate.query(
                "SELECT id, segment_id, price, currency, recorded_at, purchased FROM price_records " +
                "WHERE segment_id = ? ORDER BY recorded_at DESC, id DESC",
                ROW_MAPPER, segmentId
        );
    }

    @Override
    public Optional<PriceRecord> findLowestBySegmentId(Long segmentId) {
        List<PriceRecord> results = jdbcTemplate.query(
                "SELECT id, segment_id, price, currency, recorded_at, purchased FROM price_records " +
                "WHERE segment_id = ? ORDER BY price ASC LIMIT 1",
                ROW_MAPPER, segmentId
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<PriceRecord> findLatestBySegmentId(Long segmentId) {
        List<PriceRecord> results = jdbcTemplate.query(
                "SELECT id, segment_id, price, currency, recorded_at, purchased FROM price_records " +
                "WHERE segment_id = ? ORDER BY recorded_at DESC, id DESC LIMIT 1",
                ROW_MAPPER, segmentId
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<PriceRecord> findById(Long id) {
        List<PriceRecord> results = jdbcTemplate.query(
                "SELECT id, segment_id, price, currency, recorded_at, purchased FROM price_records WHERE id = ?",
                ROW_MAPPER, id
        );
        return results.stream().findFirst();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM price_records WHERE id = ?", id);
    }

    @Override
    public PriceRecord update(PriceRecord record) {
        jdbcTemplate.update(
                "UPDATE price_records SET price = ?, currency = ?, recorded_at = ?, purchased = ? WHERE id = ?",
                record.getPrice().amount(),
                record.getPrice().currency().name(),
                Timestamp.from(record.getRecordedAt()),
                record.isPurchased(),
                record.getId()
        );
        return record;
    }

    @Override
    public Optional<PriceRecord> findPurchasedBySegmentId(Long segmentId) {
        List<PriceRecord> results = jdbcTemplate.query(
                "SELECT id, segment_id, price, currency, recorded_at, purchased FROM price_records " +
                "WHERE segment_id = ? AND purchased = TRUE LIMIT 1",
                ROW_MAPPER, segmentId
        );
        return results.stream().findFirst();
    }

    @Override
    public void markAsPurchased(Long priceId, Long segmentId) {
        jdbcTemplate.update("UPDATE price_records SET purchased = FALSE WHERE segment_id = ?", segmentId);
        jdbcTemplate.update("UPDATE price_records SET purchased = TRUE WHERE id = ? AND segment_id = ?", priceId, segmentId);
    }

    @Override
    public void unmarkAsPurchased(Long priceId) {
        jdbcTemplate.update("UPDATE price_records SET purchased = FALSE WHERE id = ?", priceId);
    }
}
