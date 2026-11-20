package com.medislot.config;

import com.medislot.model.Department;
import com.medislot.model.Doctor;
import com.medislot.model.DoctorSchedule;
import com.medislot.model.Patient;
import com.medislot.model.User;
import com.medislot.model.enums.DoctorStatus;
import com.medislot.model.enums.Role;
import com.medislot.repository.DepartmentRepository;
import com.medislot.repository.DoctorRepository;
import com.medislot.repository.DoctorScheduleRepository;
import com.medislot.repository.PatientRepository;
import com.medislot.repository.UserRepository;
import com.medislot.service.DoctorScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Automatically initializes database schema and realistic sample healthcare data.
 * Generates forward-looking consultation slots for immediate live demonstration.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorScheduleService scheduleService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DataSource dataSource,
                           JdbcTemplate jdbcTemplate,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           DoctorScheduleRepository scheduleRepository,
                           DoctorScheduleService scheduleService,
                           PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.scheduleRepository = scheduleRepository;
        this.scheduleService = scheduleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting MediSlot database initialization...");

        // 1. Run schema.sql
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.setContinueOnError(true);
            populator.execute(dataSource);
            log.info("Database schema verified.");
        } catch (Exception e) {
            log.warn("Schema initialization note: {}", e.getMessage());
        }

        // 2. Check if users table already has data
        int userCount = 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            if (count != null) {
                userCount = count;
            }
        } catch (Exception e) {
            log.warn("Could not query users table directly: {}", e.getMessage());
        }

        if (userCount > 0) {
            log.info("Database already seeded with {} users. Ensuring future slots exist...", userCount);
            ensureFutureSlots();
            return;
        }

        log.info("Seeding initial fictional healthcare data and demo accounts...");

        // 3. Create Departments
        Department cardiology = new Department(null, "Cardiology", "CARD", "Heart & Cardiovascular Care", "Dr. Arjun Mehta", LocalDateTime.now());
        Department dermatology = new Department(null, "Dermatology", "DERM", "Skin, Hair and Cosmetic Health", "Dr. Priya Sharma", LocalDateTime.now());
        Department genMedicine = new Department(null, "General Medicine", "GENMED", "Primary Care & Preventative Medicine", "Dr. Rahul Verma", LocalDateTime.now());
        Department orthopedics = new Department(null, "Orthopedics", "ORTHO", "Bone, Joint and Musculoskeletal Care", "Dr. Sarah Jenkins", LocalDateTime.now());
        Department pediatrics = new Department(null, "Pediatrics", "PED", "Comprehensive Child and Infant Health", "Dr. Vikram Seth", LocalDateTime.now());
        Department neurology = new Department(null, "Neurology", "NEURO", "Brain, Nerve and Spinal Conditions", "Dr. Ananya Roy", LocalDateTime.now());

        departmentRepository.save(cardiology);
        departmentRepository.save(dermatology);
        departmentRepository.save(genMedicine);
        departmentRepository.save(orthopedics);
        departmentRepository.save(pediatrics);
        departmentRepository.save(neurology);

        // 4. Create Demo Admin
        User adminUser = new User() {
            @Override public String getDashboardUrl() { return "/admin/dashboard"; }
            @Override public String getRoleDescription() { return "Hospital Administrator"; }
        };
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@medislot.org");
        adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setFullName("Marcus Aurelius");
        adminUser.setPhone("+1 (555) 010-0001");
        adminUser.setActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(adminUser);

        // 5. Create Demo Receptionist
        User recepUser = new User() {
            @Override public String getDashboardUrl() { return "/receptionist/dashboard"; }
            @Override public String getRoleDescription() { return "Front Desk Receptionist"; }
        };
        recepUser.setUsername("receptionist");
        recepUser.setEmail("reception@medislot.org");
        recepUser.setPasswordHash(passwordEncoder.encode("recep123"));
        recepUser.setRole(Role.RECEPTIONIST);
        recepUser.setFullName("Clara Oswald");
        recepUser.setPhone("+1 (555) 010-0002");
        recepUser.setActive(true);
        recepUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(recepUser);

        // 6. Create Doctors
        createDoctorSeed("dr.arjun", "arjun.mehta@medislot.org", "doctor123", "Arjun Mehta",
                "+1 (555) 101-2001", cardiology.getDepartmentId(), "Cardiologist",
                "MBBS, MD (Cardiology), FACC", 14, BigDecimal.valueOf(800.00));

        createDoctorSeed("dr.priya", "priya.sharma@medislot.org", "doctor123", "Priya Sharma",
                "+1 (555) 101-2002", dermatology.getDepartmentId(), "Dermatologist",
                "MBBS, DVD, MD (Dermatology)", 9, BigDecimal.valueOf(650.00));

        createDoctorSeed("dr.rahul", "rahul.verma@medislot.org", "doctor123", "Rahul Verma",
                "+1 (555) 101-2003", genMedicine.getDepartmentId(), "Internal Medicine Consultant",
                "MBBS, MD (General Medicine)", 16, BigDecimal.valueOf(500.00));

        createDoctorSeed("dr.sarah", "sarah.jenkins@medislot.org", "doctor123", "Sarah Jenkins",
                "+1 (555) 101-2004", orthopedics.getDepartmentId(), "Orthopedic Surgeon",
                "MBBS, MS (Ortho), FRCS", 12, BigDecimal.valueOf(750.00));

        // 7. Create Demo Patients
        createPatientSeed("eleanor", "eleanor.vance@example.com", "patient123", "Eleanor Vance",
                "+1 (555) 301-8891", "PAT-202611-1001", LocalDate.of(1992, 5, 14),
                "Female", "A+", "742 Evergreen Terrace, Springfield", "+1 (555) 301-9999");

        createPatientSeed("marcus", "marcus.brody@example.com", "patient123", "Marcus Brody",
                "+1 (555) 301-4422", "PAT-202611-1002", LocalDate.of(1985, 9, 28),
                "Male", "O-", "124 Conch Street, Riverdale", "+1 (555) 301-1122");

        createPatientSeed("sophia", "sophia.lin@example.com", "patient123", "Sophia Lin",
                "+1 (555) 301-7711", "PAT-202611-1003", LocalDate.of(2001, 2, 10),
                "Female", "B+", "42 Wallaby Way, Sydney Park", "+1 (555) 301-3344");

        // 8. Generate Forward Slots for next 2 weeks
        ensureFutureSlots();

        log.info("MediSlot database initialization and seeding completed successfully!");
    }

    private void createDoctorSeed(String username, String email, String rawPassword, String fullName,
                                  String phone, Long deptId, String specialization,
                                  String qualification, int experienceYears, BigDecimal fee) {
        Doctor user = new Doctor();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(Role.DOCTOR);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        Long userId = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(userId);
        doctor.setDepartmentId(deptId);
        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setExperienceYears(experienceYears);
        doctor.setConsultationFee(fee);
        doctor.setStatus(DoctorStatus.ACTIVE);
        Long docId = doctorRepository.save(doctor);

        // Add standard weekly schedule (Monday through Friday 09:00 - 13:00)
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
        for (DayOfWeek day : days) {
            DoctorSchedule sched = new DoctorSchedule(null, docId, day,
                    LocalTime.of(9, 0), LocalTime.of(13, 0), 30, true);
            scheduleRepository.save(sched);
        }
    }

    private void createPatientSeed(String username, String email, String rawPassword, String fullName,
                                   String phone, String patientCode, LocalDate dob,
                                   String gender, String bloodGroup, String address, String emergencyContact) {
        Patient user = new Patient();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(Role.PATIENT);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        Long userId = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setPatientCode(patientCode);
        patient.setFullName(fullName);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setDateOfBirth(dob);
        patient.setGender(gender);
        patient.setBloodGroup(bloodGroup);
        patient.setAddress(address);
        patient.setEmergencyContact(emergencyContact);
        patient.setRegistrationDate(LocalDateTime.now().minusDays(15));
        patientRepository.save(patient);
    }

    private void ensureFutureSlots() {
        for (Doctor doc : doctorRepository.findAll()) {
            scheduleService.generateSlotsForDoctor(doc.getDoctorId(), LocalDate.now(), 14);
        }
    }
}