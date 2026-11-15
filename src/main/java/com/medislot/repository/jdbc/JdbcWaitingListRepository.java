package com.medislot.repository.jdbc;

import com.medislot.model.WaitingListEntry;
import com.medislot.model.enums.WaitingStatus;
import com.medislot.repository.WaitingListRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcWaitingListRepository implements WaitingListRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingListRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<WaitingListEntry> waitingRowMapper = (rs, rowNum) -> {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setWaitingId(rs.getLong("waiting_id"));
        entry.setDoctorId(rs.getLong("doctor_id"));
        entry.setPatientId(rs.getLong("patient_id"));

        Date rd = rs.getDate("requested_date");
        if (rd != null) entry.setRequestedDate(rd.toLocalDate());

        entry.setQueuePosition(rs.getInt("queue_position"));
        entry.setStatus(WaitingStatus.valueOf(rs.getString("status")));
        entry.setNotes(rs.getString("notes"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) entry.setCreatedAt(ts.toLocalDateTime());

        try {
            entry.setDoctorName(rs.getString("doctor_name"));
            entry.setPatientName(rs.getString("patient_name"));
            entry.setPatientPhone(rs.getString("patient_phone"));
        } catch (Exception ignored) {}

        return entry;
    };

    private static final String BASE_SELECT =
            "SELECT w.*, du.full_name AS doctor_name, p.full_name AS patient_name, p.phone AS patient_phone " +
            "FROM waiting_list w " +
            "JOIN doctors d ON w.doctor_id = d.doctor_id " +
            "JOIN users du ON d.user_id = du.user_id " +
            "JOIN patients p ON w.patient_id = p.patient_id ";

    @Override
    public Optional<WaitingListEntry> findById(Long waitingId) {
        String sql = BASE_SELECT + "WHERE w.waiting_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, waitingRowMapper, waitingId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<WaitingListEntry> findByDoctorAndDate(Long doctorId, LocalDate date) {
        String sql = BASE_SELECT + "WHERE w.doctor_id = ? AND w.requested_date = ? AND w.status = 'WAITING' ORDER BY w.queue_position ASC";
        return jdbcTemplate.query(sql, waitingRowMapper, doctorId, Date.valueOf(date));
    }

    @Override
    public List<WaitingListEntry> findActiveQueue() {
        String sql = BASE_SELECT + "WHERE w.status = 'WAITING' ORDER BY w.requested_date ASC, w.queue_position ASC";
        return jdbcTemplate.query(sql, waitingRowMapper);
    }

    @Override
    public Long save(WaitingListEntry entry) {
        String sql = "INSERT INTO waiting_list (doctor_id, patient_id, requested_date, queue_position, status, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, entry.getDoctorId());
            ps.setLong(2, entry.getPatientId());
            ps.setDate(3, Date.valueOf(entry.getRequestedDate()));
            ps.setInt(4, entry.getQueuePosition());
            ps.setString(5, entry.getStatus().name());
            ps.setString(6, entry.getNotes());
            ps.setTimestamp(7, Timestamp.valueOf(entry.getCreatedAt() != null ? entry.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            entry.setWaitingId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int updateStatus(Long waitingId, WaitingStatus status) {
        String sql = "UPDATE waiting_list SET status = ? WHERE waiting_id = ?";
        return jdbcTemplate.update(sql, status.name(), waitingId);
    }

    @Override
    public int getNextQueuePosition(Long doctorId, LocalDate date) {
        String sql = "SELECT COALESCE(MAX(queue_position), 0) + 1 FROM waiting_list WHERE doctor_id = ? AND requested_date = ?";
        Integer pos = jdbcTemplate.queryForObject(sql, Integer.class, doctorId, Date.valueOf(date));
        return pos != null ? pos : 1;
    }

    @Override
    public long countWaitingEntries() {
        String sql = "SELECT COUNT(*) FROM waiting_list WHERE status = 'WAITING'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
