package com.medislot.repository.jdbc;

import com.medislot.model.AuditLog;
import com.medislot.repository.AuditLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcAuditLogRepository implements AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AuditLog> logRowMapper = (rs, rowNum) -> {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getLong("log_id"));

        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) log.setUserId(uid);

        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getString("entity_id"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());

        try {
            log.setUsername(rs.getString("username"));
        } catch (Exception ignored) {}

        return log;
    };

    @Override
    public Long save(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, ip_address, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (log.getUserId() != null) {
                ps.setLong(1, log.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityType());
            ps.setString(4, log.getEntityId());
            ps.setString(5, log.getDetails());
            ps.setString(6, log.getIpAddress());
            ps.setTimestamp(7, Timestamp.valueOf(log.getCreatedAt() != null ? log.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            log.setLogId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        String sql = "SELECT l.*, u.username " +
                     "FROM audit_logs l " +
                     "LEFT JOIN users u ON l.user_id = u.user_id " +
                     "ORDER BY l.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, logRowMapper, limit);
    }

    @Override
    public List<AuditLog> findByEntityType(String entityType, String entityId) {
        String sql = "SELECT l.*, u.username " +
                     "FROM audit_logs l " +
                     "LEFT JOIN users u ON l.user_id = u.user_id " +
                     "WHERE l.entity_type = ? AND l.entity_id = ? " +
                     "ORDER BY l.created_at DESC";
        return jdbcTemplate.query(sql, logRowMapper, entityType, entityId);
    }
}
