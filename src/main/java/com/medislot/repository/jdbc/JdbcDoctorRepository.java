package com.medislot.repository.jdbc;

import com.medislot.model.Doctor;
import com.medislot.model.enums.DoctorStatus;
import com.medislot.model.enums.Role;
import com.medislot.repository.DoctorRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDoctorRepository implements DoctorRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDoctorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Doctor> doctorRowMapper = (rs, rowNum) -> {
        Doctor doc = new Doctor();
        doc.setDoctorId(rs.getLong("doctor_id"));
        doc.setUserId(rs.getLong("user_id"));
        doc.setDepartmentId(rs.getLong("department_id"));
        doc.setDepartmentName(rs.getString("department_name"));
        doc.setSpecialization(rs.getString("specialization"));
        doc.setQualification(rs.getString("qualification"));
        doc.setExperienceYears(rs.getInt("experience_years"));
        doc.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        doc.setStatus(DoctorStatus.valueOf(rs.getString("status")));

        // Joined User fields
        doc.setUsername(rs.getString("username"));
        doc.setEmail(rs.getString("email"));
        doc.setFullName(rs.getString("full_name"));
        doc.setPhone(rs.getString("phone"));
        doc.setActive(rs.getBoolean("is_active"));
        doc.setRole(Role.DOCTOR);

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            doc.setCreatedAt(ts.toLocalDateTime());
        }
        return doc;
    };

    private static final String BASE_SELECT =
            "SELECT d.doctor_id, d.user_id, d.department_id, d.specialization, d.qualification, " +
            "d.experience_years, d.consultation_fee, d.status, " +
            "u.username, u.email, u.full_name, u.phone, u.is_active, u.created_at, " +
            "dept.name AS department_name " +
            "FROM doctors d " +
            "JOIN users u ON d.user_id = u.user_id " +
            "JOIN departments dept ON d.department_id = dept.department_id ";

    @Override
    public Optional<Doctor> findById(Long doctorId) {
        String sql = BASE_SELECT + "WHERE d.doctor_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, doctorRowMapper, doctorId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Doctor> findByUserId(Long userId) {
        String sql = BASE_SELECT + "WHERE d.user_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, doctorRowMapper, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Doctor> findAll() {
        String sql = BASE_SELECT + "ORDER BY d.doctor_id ASC";
        return jdbcTemplate.query(sql, doctorRowMapper);
    }

    @Override
    public List<Doctor> findByDepartmentId(Long departmentId) {
        String sql = BASE_SELECT + "WHERE d.department_id = ? AND d.status = 'ACTIVE' ORDER BY u.full_name ASC";
        return jdbcTemplate.query(sql, doctorRowMapper, departmentId);
    }

    @Override
    public List<Doctor> searchDoctors(String query, Long departmentId) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (departmentId != null && departmentId > 0) {
            sql.append("AND d.department_id = ? ");
            params.add(departmentId);
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (u.full_name LIKE ? OR d.specialization LIKE ? OR dept.name LIKE ?) ");
            String term = "%" + query.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY u.full_name ASC");
        return jdbcTemplate.query(sql.toString(), doctorRowMapper, params.toArray());
    }

    @Override
    public Long save(Doctor doctor) {
        String sql = "INSERT INTO doctors (user_id, department_id, specialization, qualification, experience_years, consultation_fee, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, doctor.getUserId());
            ps.setLong(2, doctor.getDepartmentId());
            ps.setString(3, doctor.getSpecialization());
            ps.setString(4, doctor.getQualification());
            ps.setInt(5, doctor.getExperienceYears());
            ps.setBigDecimal(6, doctor.getConsultationFee());
            ps.setString(7, doctor.getStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            doctor.setDoctorId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int update(Doctor doctor) {
        String sql = "UPDATE doctors SET department_id = ?, specialization = ?, qualification = ?, " +
                     "experience_years = ?, consultation_fee = ?, status = ? WHERE doctor_id = ?";
        return jdbcTemplate.update(sql, doctor.getDepartmentId(), doctor.getSpecialization(),
                doctor.getQualification(), doctor.getExperienceYears(), doctor.getConsultationFee(),
                doctor.getStatus().name(), doctor.getDoctorId());
    }

    @Override
    public int updateStatus(Long doctorId, String status) {
        String sql = "UPDATE doctors SET status = ? WHERE doctor_id = ?";
        return jdbcTemplate.update(sql, status, doctorId);
    }

    @Override
    public long countActiveDoctors() {
        String sql = "SELECT COUNT(*) FROM doctors WHERE status = 'ACTIVE'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
