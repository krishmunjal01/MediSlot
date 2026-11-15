package com.medislot.repository.jdbc;

import com.medislot.model.DoctorSchedule;
import com.medislot.repository.DoctorScheduleRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDoctorScheduleRepository implements DoctorScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDoctorScheduleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<DoctorSchedule> scheduleRowMapper = (rs, rowNum) -> {
        DoctorSchedule sched = new DoctorSchedule();
        sched.setScheduleId(rs.getLong("schedule_id"));
        sched.setDoctorId(rs.getLong("doctor_id"));
        sched.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week")));

        Time st = rs.getTime("start_time");
        if (st != null) sched.setStartTime(st.toLocalTime());

        Time et = rs.getTime("end_time");
        if (et != null) sched.setEndTime(et.toLocalTime());

        sched.setSlotDurationMinutes(rs.getInt("slot_duration_minutes"));
        sched.setActive(rs.getBoolean("is_active"));

        try {
            sched.setDoctorName(rs.getString("doctor_name"));
        } catch (Exception ignored) {}

        return sched;
    };

    private static final String BASE_SELECT =
            "SELECT s.*, u.full_name AS doctor_name " +
            "FROM doctor_schedules s " +
            "JOIN doctors d ON s.doctor_id = d.doctor_id " +
            "JOIN users u ON d.user_id = u.user_id ";

    @Override
    public Optional<DoctorSchedule> findById(Long scheduleId) {
        String sql = BASE_SELECT + "WHERE s.schedule_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, scheduleRowMapper, scheduleId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<DoctorSchedule> findByDoctorId(Long doctorId) {
        String sql = BASE_SELECT + "WHERE s.doctor_id = ? AND s.is_active = TRUE ORDER BY s.schedule_id ASC";
        return jdbcTemplate.query(sql, scheduleRowMapper, doctorId);
    }

    @Override
    public Optional<DoctorSchedule> findByDoctorAndDay(Long doctorId, DayOfWeek dayOfWeek) {
        String sql = BASE_SELECT + "WHERE s.doctor_id = ? AND s.day_of_week = ? AND s.is_active = TRUE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, scheduleRowMapper, doctorId, dayOfWeek.name()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<DoctorSchedule> findAll() {
        String sql = BASE_SELECT + "ORDER BY s.doctor_id ASC, s.schedule_id ASC";
        return jdbcTemplate.query(sql, scheduleRowMapper);
    }

    @Override
    public Long save(DoctorSchedule schedule) {
        String sql = "INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, slot_duration_minutes, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, schedule.getDoctorId());
            ps.setString(2, schedule.getDayOfWeek().name());
            ps.setTime(3, Time.valueOf(schedule.getStartTime()));
            ps.setTime(4, Time.valueOf(schedule.getEndTime()));
            ps.setInt(5, schedule.getSlotDurationMinutes());
            ps.setBoolean(6, schedule.isActive());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            schedule.setScheduleId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int update(DoctorSchedule schedule) {
        String sql = "UPDATE doctor_schedules SET day_of_week = ?, start_time = ?, end_time = ?, " +
                     "slot_duration_minutes = ?, is_active = ? WHERE schedule_id = ?";
        return jdbcTemplate.update(sql, schedule.getDayOfWeek().name(), Time.valueOf(schedule.getStartTime()),
                Time.valueOf(schedule.getEndTime()), schedule.getSlotDurationMinutes(), schedule.isActive(), schedule.getScheduleId());
    }

    @Override
    public int deleteById(Long scheduleId) {
        String sql = "DELETE FROM doctor_schedules WHERE schedule_id = ?";
        return jdbcTemplate.update(sql, scheduleId);
    }
}
