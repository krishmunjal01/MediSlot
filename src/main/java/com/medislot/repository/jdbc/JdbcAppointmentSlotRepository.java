package com.medislot.repository.jdbc;

import com.medislot.model.AppointmentSlot;
import com.medislot.model.enums.SlotStatus;
import com.medislot.repository.AppointmentSlotRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAppointmentSlotRepository implements AppointmentSlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAppointmentSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AppointmentSlot> slotRowMapper = (rs, rowNum) -> {
        AppointmentSlot slot = new AppointmentSlot();
        slot.setSlotId(rs.getLong("slot_id"));
        slot.setDoctorId(rs.getLong("doctor_id"));

        Date d = rs.getDate("slot_date");
        if (d != null) slot.setSlotDate(d.toLocalDate());

        Time st = rs.getTime("start_time");
        if (st != null) slot.setStartTime(st.toLocalTime());

        Time et = rs.getTime("end_time");
        if (et != null) slot.setEndTime(et.toLocalTime());

        slot.setStatus(SlotStatus.valueOf(rs.getString("status")));

        try {
            slot.setDoctorName(rs.getString("doctor_name"));
            slot.setDepartmentName(rs.getString("department_name"));
        } catch (Exception ignored) {}

        return slot;
    };

    private static final String BASE_SELECT =
            "SELECT s.*, u.full_name AS doctor_name, dept.name AS department_name " +
            "FROM appointment_slots s " +
            "JOIN doctors d ON s.doctor_id = d.doctor_id " +
            "JOIN users u ON d.user_id = u.user_id " +
            "JOIN departments dept ON d.department_id = dept.department_id ";

    @Override
    public Optional<AppointmentSlot> findById(Long slotId) {
        String sql = BASE_SELECT + "WHERE s.slot_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, slotRowMapper, slotId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * CRITICAL FOR CONCURRENCY:
     * Acquires a pessimistic row-level exclusive lock (X lock) on the target slot.
     * Prevents race conditions during concurrent booking requests.
     */
    @Override
    public Optional<AppointmentSlot> findByIdForUpdate(Long slotId) {
        String sql = "SELECT s.*, u.full_name AS doctor_name, dept.name AS department_name " +
                     "FROM appointment_slots s " +
                     "JOIN doctors d ON s.doctor_id = d.doctor_id " +
                     "JOIN users u ON d.user_id = u.user_id " +
                     "JOIN departments dept ON d.department_id = dept.department_id " +
                     "WHERE s.slot_id = ? FOR UPDATE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, slotRowMapper, slotId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AppointmentSlot> findSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        String sql = BASE_SELECT + "WHERE s.doctor_id = ? AND s.slot_date = ? ORDER BY s.start_time ASC";
        return jdbcTemplate.query(sql, slotRowMapper, doctorId, Date.valueOf(date));
    }

    @Override
    public List<AppointmentSlot> findAvailableSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        String sql = BASE_SELECT + "WHERE s.doctor_id = ? AND s.slot_date = ? AND s.status = 'AVAILABLE' ORDER BY s.start_time ASC";
        return jdbcTemplate.query(sql, slotRowMapper, doctorId, Date.valueOf(date));
    }

    @Override
    public List<AppointmentSlot> findAvailableSlotsByDepartmentAndDate(Long departmentId, LocalDate date) {
        String sql = BASE_SELECT + "WHERE d.department_id = ? AND s.slot_date = ? AND s.status = 'AVAILABLE' ORDER BY s.start_time ASC";
        return jdbcTemplate.query(sql, slotRowMapper, departmentId, Date.valueOf(date));
    }

    @Override
    public boolean existsByDoctorDateAndTime(Long doctorId, LocalDate date, LocalTime startTime) {
        String sql = "SELECT COUNT(*) FROM appointment_slots WHERE doctor_id = ? AND slot_date = ? AND start_time = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, doctorId, Date.valueOf(date), Time.valueOf(startTime));
        return count != null && count > 0;
    }

    @Override
    public Long save(AppointmentSlot slot) {
        String sql = "INSERT INTO appointment_slots (doctor_id, slot_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, slot.getDoctorId());
            ps.setDate(2, Date.valueOf(slot.getSlotDate()));
            ps.setTime(3, Time.valueOf(slot.getStartTime()));
            ps.setTime(4, Time.valueOf(slot.getEndTime()));
            ps.setString(5, slot.getStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            slot.setSlotId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int batchInsertSlots(List<AppointmentSlot> slots) {
        String sql = "INSERT IGNORE INTO appointment_slots (doctor_id, slot_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?)";
        int[][] results = jdbcTemplate.batchUpdate(sql, slots, slots.size(), (ps, slot) -> {
            ps.setLong(1, slot.getDoctorId());
            ps.setDate(2, Date.valueOf(slot.getSlotDate()));
            ps.setTime(3, Time.valueOf(slot.getStartTime()));
            ps.setTime(4, Time.valueOf(slot.getEndTime()));
            ps.setString(5, slot.getStatus().name());
        });
        int total = 0;
        for (int[] r : results) {
            for (int count : r) {
                if (count > 0) total += count;
            }
        }
        return total;
    }

    @Override
    public int updateStatus(Long slotId, SlotStatus status) {
        String sql = "UPDATE appointment_slots SET status = ? WHERE slot_id = ?";
        return jdbcTemplate.update(sql, status.name(), slotId);
    }

    @Override
    public int deleteUnbookedSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        String sql = "DELETE FROM appointment_slots WHERE doctor_id = ? AND slot_date = ? AND status = 'AVAILABLE'";
        return jdbcTemplate.update(sql, doctorId, Date.valueOf(date));
    }
}