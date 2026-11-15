package com.medislot.controller;

import com.medislot.config.CustomUserDetails;
import com.medislot.dto.DoctorFormDto;
import com.medislot.dto.ScheduleFormDto;
import com.medislot.model.Appointment;
import com.medislot.model.AppointmentSummaryPOJO;
import com.medislot.model.Department;
import com.medislot.model.Doctor;
import com.medislot.model.Patient;
import com.medislot.repository.traditional.TraditionalPatientJdbcRepository;
import com.medislot.service.AppointmentService;
import com.medislot.service.DashboardService;
import com.medislot.service.DepartmentService;
import com.medislot.service.DoctorScheduleService;
import com.medislot.service.DoctorService;
import com.medislot.service.PatientRecordService;
import com.medislot.service.PatientService;
import com.medislot.service.WaitingListService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final DepartmentService departmentService;
    private final DoctorScheduleService scheduleService;
    private final AppointmentService appointmentService;
    private final WaitingListService waitingListService;
    private final PatientRecordService patientRecordService;
    private final TraditionalPatientJdbcRepository traditionalPatientJdbcRepository;

    public AdminController(DashboardService dashboardService,
                           DoctorService doctorService,
                           PatientService patientService,
                           DepartmentService departmentService,
                           DoctorScheduleService scheduleService,
                           AppointmentService appointmentService,
                           WaitingListService waitingListService,
                           PatientRecordService patientRecordService,
                           TraditionalPatientJdbcRepository traditionalPatientJdbcRepository) {
        this.dashboardService = dashboardService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.departmentService = departmentService;
        this.scheduleService = scheduleService;
        this.appointmentService = appointmentService;
        this.waitingListService = waitingListService;
        this.patientRecordService = patientRecordService;
        this.traditionalPatientJdbcRepository = traditionalPatientJdbcRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("currentUser", user.getUser());
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("todayAppointments", appointmentService.getAppointmentsForToday());
        model.addAttribute("upcomingAppointments", appointmentService.getUpcomingAppointments(5));
        model.addAttribute("statusDistribution", dashboardService.getAppointmentStatusDistribution());
        return "admin/dashboard";
    }

    // -------------------------------------------------------------
    // Doctor Management
    // -------------------------------------------------------------
    @GetMapping("/doctors")
    public String listDoctors(@RequestParam(value = "query", required = false) String query,
                              @RequestParam(value = "departmentId", required = false) Long departmentId,
                              Model model) {
        model.addAttribute("doctors", doctorService.searchDoctors(query, departmentId));
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("selectedQuery", query);
        model.addAttribute("selectedDept", departmentId);
        return "admin/doctors";
    }

    @GetMapping("/doctors/new")
    public String newDoctorForm(Model model) {
        model.addAttribute("doctorForm", new DoctorFormDto());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/doctor-form";
    }

    @PostMapping("/doctors/save")
    public String saveDoctor(@Valid @ModelAttribute("doctorForm") DoctorFormDto form,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails user,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/doctor-form";
        }
        try {
            if (form.getDoctorId() != null) {
                doctorService.updateDoctor(form, user.getUserId());
                redirectAttributes.addFlashAttribute("successMessage", "Doctor profile updated successfully.");
            } else {
                doctorService.createDoctor(form, user.getUserId());
                redirectAttributes.addFlashAttribute("successMessage", "New doctor created and registered successfully.");
            }
            return "redirect:/admin/doctors";
        } catch (Exception e) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/doctor-form";
        }
    }

    @GetMapping("/doctors/edit/{id}")
    public String editDoctor(@PathVariable("id") Long doctorId, Model model) {
        Doctor doc = doctorService.getDoctorById(doctorId);
        DoctorFormDto dto = new DoctorFormDto();
        dto.setDoctorId(doc.getDoctorId());
        dto.setUserId(doc.getUserId());
        dto.setUsername(doc.getUsername());
        dto.setFullName(doc.getFullName());
        dto.setEmail(doc.getEmail());
        dto.setPhone(doc.getPhone());
        dto.setDepartmentId(doc.getDepartmentId());
        dto.setSpecialization(doc.getSpecialization());
        dto.setQualification(doc.getQualification());
        dto.setExperienceYears(doc.getExperienceYears());
        dto.setConsultationFee(doc.getConsultationFee());
        dto.setStatus(doc.getStatus().name());

        model.addAttribute("doctorForm", dto);
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/doctor-form";
    }

    @PostMapping("/doctors/{id}/status")
    public String toggleDoctorStatus(@PathVariable("id") Long doctorId,
                                     @RequestParam("status") String status,
                                     @AuthenticationPrincipal CustomUserDetails user,
                                     RedirectAttributes redirectAttributes) {
        doctorService.updateDoctorStatus(doctorId, status, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Doctor status updated to " + status + ".");
        return "redirect:/admin/doctors";
    }

    // -------------------------------------------------------------
    // Patient Management
    // -------------------------------------------------------------
    @GetMapping("/patients")
    public String listPatients(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Patient> patients = (search != null && !search.trim().isEmpty()) ?
                patientService.searchPatients(search) : patientService.getAllPatients();
        model.addAttribute("patients", patients);
        model.addAttribute("searchTerm", search);
        return "admin/patients";
    }

    @GetMapping("/patients/{id}")
    public String viewPatientDetails(@PathVariable("id") Long patientId, Model model) {
        Patient patient = patientService.getPatientById(patientId);
        List<Appointment> appointments = appointmentService.getAppointmentsForPatient(patientId);
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("medicalRecords", patientRecordService.getRecordsForPatient(patientId));
        return "admin/patient-details";
    }

    // -------------------------------------------------------------
    // Department Management
    // -------------------------------------------------------------
    @GetMapping("/departments")
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("newDepartment", new Department());
        return "admin/departments";
    }

    @PostMapping("/departments/save")
    public String saveDepartment(@ModelAttribute("newDepartment") Department dept,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 RedirectAttributes redirectAttributes) {
        departmentService.saveDepartment(dept, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Department saved successfully.");
        return "redirect:/admin/departments";
    }

    // -------------------------------------------------------------
    // Doctor Schedules
    // -------------------------------------------------------------
    @GetMapping("/schedules")
    public String listSchedules(Model model) {
        model.addAttribute("schedules", scheduleService.getAllSchedules());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("scheduleForm", new ScheduleFormDto());
        return "admin/schedules";
    }

    @PostMapping("/schedules/save")
    public String saveSchedule(@Valid @ModelAttribute("scheduleForm") ScheduleFormDto form,
                               BindingResult result,
                               @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("schedules", scheduleService.getAllSchedules());
            model.addAttribute("doctors", doctorService.getAllDoctors());
            return "admin/schedules";
        }
        try {
            scheduleService.saveSchedule(form, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Schedule saved and slots generated successfully.");
            return "redirect:/admin/schedules";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/schedules";
        }
    }

    @PostMapping("/schedules/generate")
    public String generateSlotsManually(@RequestParam("doctorId") Long doctorId,
                                        @RequestParam("weeksAhead") int weeksAhead,
                                        RedirectAttributes redirectAttributes) {
        int count = scheduleService.generateSlotsForDoctor(doctorId, LocalDate.now(), weeksAhead * 7);
        redirectAttributes.addFlashAttribute("successMessage", "Generated " + count + " new slots successfully.");
        return "redirect:/admin/schedules";
    }

    // -------------------------------------------------------------
    // Appointments Oversight
    // -------------------------------------------------------------
    @GetMapping("/appointments")
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return "admin/appointments";
    }

    // -------------------------------------------------------------
    // Waiting List
    // -------------------------------------------------------------
    @GetMapping("/waiting-list")
    public String waitingList(Model model) {
        model.addAttribute("queue", waitingListService.getActiveWaitingList());
        return "admin/waiting-list";
    }

    @PostMapping("/waiting-list/{id}/notify")
    public String notifyWaiting(@PathVariable("id") Long waitingId,
                                @AuthenticationPrincipal CustomUserDetails user,
                                RedirectAttributes redirectAttributes) {
        waitingListService.notifyNextInQueue(waitingId, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Notified patient of slot availability.");
        return "redirect:/admin/waiting-list";
    }

    // -------------------------------------------------------------
    // Reports & Analytics
    // -------------------------------------------------------------
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("departmentData", dashboardService.getDepartmentDistribution());
        model.addAttribute("doctorPerformance", dashboardService.getDoctorPerformanceSummary());
        model.addAttribute("statusDistribution", dashboardService.getAppointmentStatusDistribution());
        return "admin/reports";
    }

    // -------------------------------------------------------------
    // Academic Demonstrations: Traditional JDBC vs Spring JdbcTemplate
    // -------------------------------------------------------------
    @GetMapping("/technical-demonstration")
    public String technicalDemonstration(@RequestParam(value = "searchId", required = false, defaultValue = "1") Long searchId,
                                         @RequestParam(value = "searchCode", required = false) String searchCode,
                                         Model model) {

        // 1. Traditional JDBC Execution
        long tradStart = System.nanoTime();
        Optional<Patient> traditionalResult;
        if (searchCode != null && !searchCode.trim().isEmpty()) {
            traditionalResult = traditionalPatientJdbcRepository.findByPatientCode(searchCode.trim());
        } else {
            traditionalResult = traditionalPatientJdbcRepository.findById(searchId);
        }
        long tradElapsedNs = System.nanoTime() - tradStart;

        // 2. Spring JdbcTemplate Execution
        long springStart = System.nanoTime();
        Optional<Patient> springResult;
        if (searchCode != null && !searchCode.trim().isEmpty()) {
            try {
                springResult = Optional.ofNullable(patientService.getPatientByCode(searchCode.trim()));
            } catch (Exception e) {
                springResult = Optional.empty();
            }
        } else {
            try {
                springResult = Optional.ofNullable(patientService.getPatientById(searchId));
            } catch (Exception e) {
                springResult = Optional.empty();
            }
        }
        long springElapsedNs = System.nanoTime() - springStart;

        model.addAttribute("searchId", searchId);
        model.addAttribute("searchCode", searchCode);
        model.addAttribute("traditionalPatient", traditionalResult.orElse(null));
        model.addAttribute("traditionalElapsedMs", tradElapsedNs / 1_000_000.0);
        model.addAttribute("springPatient", springResult.orElse(null));
        model.addAttribute("springElapsedMs", springElapsedNs / 1_000_000.0);

        return "admin/technical-demonstration";
    }

    // -------------------------------------------------------------
    // Academic Demonstrations: POJO vs Spring-Managed Bean
    // -------------------------------------------------------------
    @GetMapping("/oop-demonstration")
    public String oopDemonstration(Model model) {
        // Demonstrate creating a POJO using the 'new' keyword:
        AppointmentSummaryPOJO pojoInstance = new AppointmentSummaryPOJO(
                "APT-20261115-DEMO",
                "Eleanor Vance",
                "Arjun Mehta",
                "Cardiology",
                LocalDate.now(),
                java.time.LocalTime.of(10, 30),
                "CONFIRMED",
                750.00
        );

        model.addAttribute("pojoInstance", pojoInstance);
        model.addAttribute("pojoSummary", pojoInstance.getFormattedSummary());
        model.addAttribute("springBeanClassName", appointmentService.getClass().getName());
        model.addAttribute("springBeanInterfaces", appointmentService.getClass().getInterfaces());

        return "admin/oop-demonstration";
    }
}
