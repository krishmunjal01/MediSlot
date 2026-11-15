package com.medislot.repository.jdbc;

import com.medislot.model.Appointment;
import com.medislot.model.enums.AppointmentStatus;
import com.medislot.repository.AppointmentRepository;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcAppointmentRepository implements AppointmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Appointment> appointmentRowMapper = (rs, rowNum) -> {
        Appointment apt = new Appointment();
        apt.setAppointmentId(rs.getLong("appointment_id"));
        apt.setAppointmentReference(rs.getString("appointment_reference"));
        apt.setPatientId(rs.getLong("patient_id"));
        apt.setDoctorId(rs.getLong("doctor_id"));
        apt.setSlotId(rs.getLong("slot_id"));

        Date ad = rs.getDate("appointment_date");
        if (ad != null) apt.setAppointmentDate(ad.toLocalDate());

        Time st = rs.getTime("start_time");
        if (st != null) apt.setStartTime(st.toLocalTime());

        Time et = rs.getTime("end_time");
        if (et != null) apt.setEndTime(et.toLocalTime());

        apt.setReasonForVisit(rs.getString("reason_for_visit"));
        apt.setStatus(AppointmentStatus.valueOf(rs.getString("status")));

        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) apt.setCreatedAt(ct.toLocalDateTime());

        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) apt.setUpdatedAt(ut.toLocalDateTime());

        // Joined fields
        try {
            apt.setPatientName(rs.getString("patient_name"));
            apt.setPatientCode(rs.getString("patient_code"));
            apt.setPatientPhone(rs.getString("patient_phone"));
            apt.setPatientEmail(rs.getString("patient_email"));
            apt.setDoctorName(rs.getString("doctor_name"));
            apt.setDoctorSpecialization(rs.getString("doctor_specialization"));
            apt.setDepartmentName(rs.getString("department_name"));
        } catch (Exception ignored) {}

        return apt;
    };

    private static final String BASE_SELECT =
            "SELECT a.*, " +
            "p.full_name AS patient_name, p.patient_code, p.phone AS patient_phone, p.email AS patient_email, " +
            "du.full_name AS doctor_name, d.specialization AS doctor_specialization, " +
            "dept.name AS department_name " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors d ON a.doctor_id = d.doctor_id " +
            "JOIN users du ON d.user_id = du.user_id " +
            "JOIN departments dept ON d.department_id = dept.department_id ";

    @Override
    public Optional<Appointment> findById(Long appointmentId) {
        String sql = BASE_SELECT + "WHERE a.appointment_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, appointmentRowMapper, appointmentId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Appointment> findByIdForUpdate(Long appointmentId) {
        String sql = BASE_SELECT + "WHERE a.appointment_id = ? FOR UPDATE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, appointmentRowMapper, appointmentId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Appointment> findByReference(String reference) {
        String sql = BASE_SELECT + "WHERE a.appointment_reference = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, appointmentRowMapper, reference));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Appointment> findBySlotId(Long slotId) {
        String sql = BASE_SELECT + "WHERE a.slot_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, appointmentRowMapper, slotId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Appointment> findByPatientId(Long patientId) {
        String sql = BASE_SELECT + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.start_time DESC";
        return jdbcTemplate.query(sql, appointmentRowMapper, patientId);
    }

    @Override
    public List<Appointment> findByDoctorId(Long doctorId) {
        String sql = BASE_SELECT + "WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC, a.start_time DESC";
        return jdbcTemplate.query(sql, appointmentRowMapper, doctorId);
    }

    @Override
    public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) {
        String sql = BASE_SELECT + "WHERE a.doctor_id = ? AND a.appointment_date = ? ORDER BY a.start_time ASC";
        return jdbcTemplate.query(sql, appointmentRowMapper, doctorId, Date.valueOf(date));
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        String sql = BASE_SELECT + "WHERE a.appointment_date = ? ORDER BY a.start_time ASC";
        return jdbcTemplate.query(sql, appointmentRowMapper, Date.valueOf(date));
    }

    @Override
    public List<Appointment> findUpcomingAppointments(int limit) {
        String sql = BASE_SELECT +
                     "WHERE a.appointment_date >= CURRENT_DATE AND a.status IN ('BOOKED', 'CONFIRMED') " +
                     "ORDER BY a.appointment_date ASC, a.start_time ASC LIMIT ?";
        return jdbcTemplate.query(sql, appointmentRowMapper, limit);
    }

    @Override
    public List<Appointment> findAll() {
        String sql = BASE_SELECT + "ORDER BY a.appointment_date DESC, a.start_time DESC";
        return jdbcTemplate.query(sql, appointmentRowMapper);
    }

    @Override
    public Long save(Appointment appointment) {
        String sql = "INSERT INTO appointments (appointment_reference, patient_id, doctor_id, slot_id, " +
                     "appointment_date, start_time, end_time, reason_for_visit, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, appointment.getAppointmentReference());
            ps.setLong(2, appointment.getPatientId());
            ps.setLong(3, appointment.getDoctorId());
            ps.setLong(4, appointment.getSlotId());
            ps.setDate(5, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(6, Time.valueOf(appointment.getStartTime()));
            ps.setTime(7, Time.valueOf(appointment.getEndTime()));
            ps.setString(8, appointment.getReasonForVisit());
            ps.setString(9, appointment.getStatus().name());
            ps.setTimestamp(10, Timestamp.valueOf(appointment.getCreatedAt() != null ? appointment.getCreatedAt() : LocalDateTime.now()));
            ps.setTimestamp(11, Timestamp.valueOf(appointment.getUpdatedAt() != null ? appointment.getUpdatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            appointment.setAppointmentId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int updateStatus(Long appointmentId, AppointmentStatus status) {
        String sql = "UPDATE appointments SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";
        return jdbcTemplate.update(sql, status.name(), appointmentId);
    }

    @Override
    public int updateSlotAndDate(Long appointmentId, Long newSlotId, LocalDate newDate, LocalTime startTime, LocalTime endTime) {
        String sql = "UPDATE appointments SET slot_id = ?, appointment_date = ?, start_time = ?, end_time = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";
        return jdbcTemplate.update(sql, newSlotId, Date.valueOf(newDate), Time.valueOf(startTime), Time.valueOf(endTime), appointmentId);
    }

    @Override
    public long countAppointmentsByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, Date.valueOf(date));
        return count != null ? count : 0L;
    }

    @Override
    public long countUpcomingAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date >= CURRENT_DATE AND status IN ('BOOKED', 'CONFIRMED')";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countCompletedAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = 'COMPLETED'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countCancelledAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = 'CANCELLED'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public Map<String, Long> countAppointmentsByStatus() {
        String sql = "SELECT status, COUNT(*) AS count FROM appointments GROUP BY status";
        Map<String, Long> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getString("status"), rs.getLong("count"));
        });
        return map;
    }

    @Override
    public List<Map<String, Object>> getDepartmentAppointmentDistribution() {
        String sql = "SELECT dept.name AS department_name, COUNT(a.appointment_id) AS appointment_count " +
                     "FROM departments dept " +
                     "LEFT JOIN doctors d ON dept.department_id = d.department_id " +
                     "LEFT JOIN appointments a ON d.doctor_id = a.doctor_id " +
                     "GROUP BY dept.department_id, dept.name " +
                     "ORDER BY appointment_count DESC";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getDoctorAppointmentSummary() {
        String sql = "SELECT u.full_name AS doctor_name, d.specialization, dept.name AS department_name, " +
                     "COUNT(a.appointment_id) AS total_appointments, " +
                     "SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count, " +
                     "SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_count " +
                     "FROM doctors d " +
                     "JOIN users u ON d.user_id = u.user_id " +
                     "JOIN departments dept ON d.department_id = dept.department_id " +
                     "LEFT JOIN appointments a ON d.doctor_id = a.doctor_id " +
                     "GROUP BY d.doctor_id, u.full_name, d.specialization, dept.name " +
                     "ORDER BY total_appointments DESC";
        return jdbcTemplate.queryForList(sql);
    }
}
