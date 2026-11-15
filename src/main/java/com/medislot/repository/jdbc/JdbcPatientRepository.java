package com.medislot.repository.jdbc;

import com.medislot.model.Patient;
import com.medislot.model.enums.Role;
import com.medislot.repository.PatientRepository;
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

/**
 * Spring JdbcTemplate implementation of PatientRepository.
 * Contrasted against TraditionalPatientJdbcRepository during technical demonstration.
 */
@Repository
public class JdbcPatientRepository implements PatientRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Patient> patientRowMapper = (rs, rowNum) -> {
        Patient p = new Patient();
        p.setPatientId(rs.getLong("patient_id"));

        long userId = rs.getLong("user_id");
        if (!rs.wasNull()) {
            p.setUserId(userId);
            p.setUsername(rs.getString("username"));
        }

        p.setPatientCode(rs.getString("patient_code"));
        p.setFullName(rs.getString("full_name"));
        p.setEmail(rs.getString("email"));
        p.setPhone(rs.getString("phone"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            p.setDateOfBirth(dob.toLocalDate());
        }

        p.setGender(rs.getString("gender"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContact(rs.getString("emergency_contact"));

        Timestamp reg = rs.getTimestamp("registration_date");
        if (reg != null) {
            p.setRegistrationDate(reg.toLocalDateTime());
        }

        p.setRole(Role.PATIENT);
        p.setActive(true);
        return p;
    };

    private static final String BASE_SELECT =
            "SELECT p.patient_id, p.user_id, p.patient_code, p.full_name, p.email, p.phone, " +
            "p.date_of_birth, p.gender, p.blood_group, p.address, p.emergency_contact, p.registration_date, " +
            "u.username " +
            "FROM patients p " +
            "LEFT JOIN users u ON p.user_id = u.user_id ";

    @Override
    public Optional<Patient> findById(Long patientId) {
        String sql = BASE_SELECT + "WHERE p.patient_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, patientRowMapper, patientId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Patient> findByUserId(Long userId) {
        String sql = BASE_SELECT + "WHERE p.user_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, patientRowMapper, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Patient> findByPatientCode(String patientCode) {
        String sql = BASE_SELECT + "WHERE p.patient_code = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, patientRowMapper, patientCode));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Patient> findByEmail(String email) {
        String sql = BASE_SELECT + "WHERE p.email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, patientRowMapper, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Patient> findByPhone(String phone) {
        String sql = BASE_SELECT + "WHERE p.phone = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, patientRowMapper, phone));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Patient> findAll() {
        String sql = BASE_SELECT + "ORDER BY p.patient_id DESC";
        return jdbcTemplate.query(sql, patientRowMapper);
    }

    @Override
    public List<Patient> searchPatients(String keyword) {
        String sql = BASE_SELECT +
                     "WHERE p.full_name LIKE ? OR p.phone LIKE ? OR p.patient_code LIKE ? OR p.email LIKE ? " +
                     "ORDER BY p.patient_id DESC";
        String term = "%" + (keyword != null ? keyword.trim() : "") + "%";
        return jdbcTemplate.query(sql, patientRowMapper, term, term, term, term);
    }

    @Override
    public Long save(Patient patient) {
        String sql = "INSERT INTO patients (user_id, patient_code, full_name, email, phone, " +
                     "date_of_birth, gender, blood_group, address, emergency_contact, registration_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (patient.getUserId() != null) {
                ps.setLong(1, patient.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, patient.getPatientCode());
            ps.setString(3, patient.getFullName());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getPhone());
            ps.setDate(6, Date.valueOf(patient.getDateOfBirth()));
            ps.setString(7, patient.getGender());
            ps.setString(8, patient.getBloodGroup());
            ps.setString(9, patient.getAddress());
            ps.setString(10, patient.getEmergencyContact());
            ps.setTimestamp(11, Timestamp.valueOf(patient.getRegistrationDate() != null ?
                    patient.getRegistrationDate() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            patient.setPatientId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int update(Patient patient) {
        String sql = "UPDATE patients SET full_name = ?, email = ?, phone = ?, date_of_birth = ?, " +
                     "gender = ?, blood_group = ?, address = ?, emergency_contact = ? WHERE patient_id = ?";
        return jdbcTemplate.update(sql, patient.getFullName(), patient.getEmail(), patient.getPhone(),
                Date.valueOf(patient.getDateOfBirth()), patient.getGender(), patient.getBloodGroup(),
                patient.getAddress(), patient.getEmergencyContact(), patient.getPatientId());
    }

    @Override
    public long countAllPatients() {
        String sql = "SELECT COUNT(*) FROM patients";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
