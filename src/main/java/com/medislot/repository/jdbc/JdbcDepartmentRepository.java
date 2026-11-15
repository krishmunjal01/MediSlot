package com.medislot.repository.jdbc;

import com.medislot.model.Department;
import com.medislot.repository.DepartmentRepository;
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
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDepartmentRepository implements DepartmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDepartmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Department> departmentRowMapper = (rs, rowNum) -> {
        Department dept = new Department();
        dept.setDepartmentId(rs.getLong("department_id"));
        dept.setName(rs.getString("name"));
        dept.setCode(rs.getString("code"));
        dept.setDescription(rs.getString("description"));
        dept.setHeadDoctorName(rs.getString("head_doctor_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            dept.setCreatedAt(ts.toLocalDateTime());
        }
        return dept;
    };

    @Override
    public Optional<Department> findById(Long departmentId) {
        String sql = "SELECT * FROM departments WHERE department_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, departmentRowMapper, departmentId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Department> findByCode(String code) {
        String sql = "SELECT * FROM departments WHERE code = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, departmentRowMapper, code));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Department> findAll() {
        String sql = "SELECT * FROM departments ORDER BY name ASC";
        return jdbcTemplate.query(sql, departmentRowMapper);
    }

    @Override
    public Long save(Department department) {
        String sql = "INSERT INTO departments (name, code, description, head_doctor_name, created_at) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, department.getName());
            ps.setString(2, department.getCode());
            ps.setString(3, department.getDescription());
            ps.setString(4, department.getHeadDoctorName());
            ps.setTimestamp(5, Timestamp.valueOf(department.getCreatedAt() != null ? department.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            department.setDepartmentId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    @Override
    public int update(Department department) {
        String sql = "UPDATE departments SET name = ?, code = ?, description = ?, head_doctor_name = ? WHERE department_id = ?";
        return jdbcTemplate.update(sql, department.getName(), department.getCode(),
                department.getDescription(), department.getHeadDoctorName(), department.getDepartmentId());
    }

    @Override
    public int deleteById(Long departmentId) {
        String sql = "DELETE FROM departments WHERE department_id = ?";
        return jdbcTemplate.update(sql, departmentId);
    }
}
