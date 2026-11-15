package com.medislot;

import com.medislot.model.Patient;
import com.medislot.repository.traditional.TraditionalPatientJdbcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TraditionalJdbcTest {

    @Autowired
    private TraditionalPatientJdbcRepository traditionalRepository;

    @Test
    @DisplayName("Traditional JDBC: Find patient by ID using Connection, PreparedStatement, ResultSet")
    void testFindByIdTraditionalJdbc() {
        Optional<Patient> patientOpt = traditionalRepository.findById(1L);
        assertTrue(patientOpt.isPresent(), "Patient with ID 1 should be found via Traditional JDBC");

        Patient patient = patientOpt.get();
        assertNotNull(patient.getFullName());
        assertNotNull(patient.getPatientCode());
        assertNotNull(patient.getEmail());
        assertTrue(patient.getAge() > 0);
    }

    @Test
    @DisplayName("Traditional JDBC: Method Overloading findByPatientCode vs findByEmailOrPhone")
    void testMethodOverloadingTraditionalJdbc() {
        Optional<Patient> byCode = traditionalRepository.findByPatientCode("PAT-202611-1001");
        assertTrue(byCode.isPresent(), "Should find patient by unique code");

        Optional<Patient> byEmail = traditionalRepository.findByEmailOrPhone("eleanor.vance@example.com");
        assertTrue(byEmail.isPresent(), "Should find patient by email using overloaded method");

        assertEquals(byCode.get().getPatientId(), byEmail.get().getPatientId(),
                "Overloaded methods should resolve to the same underlying entity");
    }

    @Test
    @DisplayName("Traditional JDBC: Search patients using LIKE queries")
    void testSearchPatientsTraditionalJdbc() {
        List<Patient> list = traditionalRepository.searchPatients("Eleanor");
        assertFalse(list.isEmpty(), "Search should return matching patient records");
        assertTrue(list.stream().anyMatch(p -> p.getFullName().contains("Eleanor")));
    }
}
