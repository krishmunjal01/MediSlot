package com.medislot.repository.jdbc;

import com.medislot.model.PatientRecord;
import com.medislot.repository.PatientRecordRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPatientRecordRepository implements PatientRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPatientRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PatientRecord> recordRowMapper = (rs, rowNum) -> {
        PatientRecord r = new PatientRecord();
        r.setRecordId(rs.getLong("record_id"));
        r.setAppointmentId(rs.getLong("appointment_id"));
        r.setPatientId(rs.getLong("patient_id"));
        r.setDoctorId(rs.getLong("doctor_id"));
        r.setSymptoms(rs.getString("symptoms"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setPrescription(rs.getString("prescription"));
        r.setClinicalNotes(rs.getString("clinical_notes"));

        Date fud = rs.getDate("follow_up_date");
        if (fud != null) r.setFollowUpDate(fud.toLocalDate());

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());

        try {
            r.setAppointmentReference(rs.getString("appointment_reference"));
            r.setPatientName(rs.getString("patient_name"));
            r.setDoctorName(rs.getString("doctor_name"));
            Date ad = rs.getDate("appointment_date");
            if (ad != null) r.setAppointmentDate(ad.toLocalDate());
        } catch (Exception ignored) {}

        return r;
    };

    private static final String BASE_SELECT =
            "SELECT pr.*, a.appointment_reference, a.appointment_date, " +
            "p.full_name AS patient_name, " +
            "du.full_name AS doctor_name " +
            "FROM patient_records pr " +
            "JOIN appointments a ON pr.appointment_id = a.appointment_id " +
            "JOIN patients p ON pr.patient_id = p.patient_id " +
            "JOIN doctors d ON pr.doctor_id = d.doctor_id " +
            "JOIN users du ON d.user_id = du.user_id ";

    @Override
    public Optional<PatientRecord> findById(Long recordId) {
        String sql = BASE_SELECT + "WHERE pr.record_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, recordRowMapper, recordId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PatientRecord> findByAppointmentId(Long appointmentId) {
        String sql = BASE_SELECT + "WHERE pr.appointment_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, recordRowMapper, appointmentId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PatientRecord> findByPatientId(Long patientId) {
        String sql = BASE_SELECT + "WHERE pr.patient_id = ? ORDER BY pr.created_at DESC";
        return jdbcTemplate.query(sql, recordRowMapper, patientId);
    }

    @Override
    public List<PatientRecord> findByDoctorId(Long doctorId) {
        String sql = BASE_SELECT + "WHERE pr.doctor_id = ? ORDER BY pr.created_at DESC";
        return jdbcTemplate.query(sql, recordRowMapper, doctorId);
    }

    @Override
    public Long save(PatientRecord record) {
        String sql = "INSERT INTO patient_records (appointment_id, patient_id, doctor_id, symptoms, diagnosis, " +
                     "prescription, clinical_notes, follow_up_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.getAppointmentId());
            ps.setLong(2, record.getPatientId());
            ps.setLong(3, record.getDoctorId());
            ps.setString(4, record.getSymptoms());
            ps.setString(5, record.getDiagnosis());
            ps.setString(6, record.getPrescription());
            ps.setString(7, record.getClinicalNotes());
            if (record.getFollowUpDate() != null) {
                ps.setDate(8, Date.valueOf(record.getFollowUpDate()));
            } else {
                ps.setNull(8, java.sql.Types.DATE);
            }
            ps.setTimestamp(9, Timestamp.valueOf(record.getCreatedAt() != null ? record.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            record.setRecordId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int update(PatientRecord record) {
        String sql = "UPDATE patient_records SET symptoms = ?, diagnosis = ?, prescription = ?, " +
                     "clinical_notes = ?, follow_up_date = ? WHERE record_id = ?";
        return jdbcTemplate.update(sql, record.getSymptoms(), record.getDiagnosis(), record.getPrescription(),
                record.getClinicalNotes(),
                record.getFollowUpDate() != null ? Date.valueOf(record.getFollowUpDate()) : null,
                record.getRecordId());
    }
}
