package com.medislot.controller;

import com.medislot.config.CustomUserDetails;
import com.medislot.dto.AppointmentBookingRequest;
import com.medislot.dto.AppointmentRescheduleRequest;
import com.medislot.dto.PatientRegistrationDto;
import com.medislot.dto.WaitingListRequest;
import com.medislot.model.Appointment;
import com.medislot.model.Doctor;
import com.medislot.model.Patient;
import com.medislot.model.enums.AppointmentStatus;
import com.medislot.service.AppointmentService;
import com.medislot.service.DepartmentService;
import com.medislot.service.DoctorScheduleService;
import com.medislot.service.DoctorService;
import com.medislot.service.PatientRecordService;
import com.medislot.service.PatientService;
import com.medislot.service.WaitingListService;
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final DoctorScheduleService scheduleService;
    private final WaitingListService waitingListService;
    private final PatientRecordService patientRecordService;

    public ReceptionistController(AppointmentService appointmentService,
                                  PatientService patientService,
                                  DoctorService doctorService,
                                  DepartmentService departmentService,
                                  DoctorScheduleService scheduleService,
                                  WaitingListService waitingListService,
                                  PatientRecordService patientRecordService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.departmentService = departmentService;
        this.scheduleService = scheduleService;
        this.waitingListService = waitingListService;
        this.patientRecordService = patientRecordService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<Appointment> todayAppointments = appointmentService.getAppointmentsForToday();
        long checkedInToday = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CHECKED_IN).count();
        long bookedToday = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.BOOKED || a.getStatus() == AppointmentStatus.CONFIRMED).count();

        model.addAttribute("currentUser", user.getUser());
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("checkedInCount", checkedInToday);
        model.addAttribute("pendingCheckInCount", bookedToday);
        model.addAttribute("waitingListCount", waitingListService.getWaitingCount());
        return "receptionist/dashboard";
    }

    // -------------------------------------------------------------
    // Patient Search & Registration
    // -------------------------------------------------------------
    @GetMapping("/patients")
    public String patients(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Patient> list = (search != null && !search.trim().isEmpty()) ?
                patientService.searchPatients(search) : patientService.getAllPatients();
        model.addAttribute("patients", list);
        model.addAttribute("searchTerm", search);
        return "receptionist/patients";
    }

    @GetMapping("/patients/new")
    public String newPatientForm(Model model) {
        model.addAttribute("patientForm", new PatientRegistrationDto());
        return "receptionist/patient-form";
    }

    @PostMapping("/patients/save")
    public String savePatient(@Valid @ModelAttribute("patientForm") PatientRegistrationDto form,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            return "receptionist/patient-form";
        }
        try {
            Patient p = patientService.registerPatient(form, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Patient " + p.getFullName() + " registered successfully with ID: " + p.getPatientCode());
            return "redirect:/receptionist/patients";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "receptionist/patient-form";
        }
    }

    @GetMapping("/patients/{id}")
    public String patientDetails(@PathVariable("id") Long patientId, Model model) {
        Patient patient = patientService.getPatientById(patientId);
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointmentService.getAppointmentsForPatient(patientId));
        model.addAttribute("records", patientRecordService.getRecordsForPatient(patientId));
        return "receptionist/patient-details";
    }

    // -------------------------------------------------------------
    // Appointment Check-in & Status
    // -------------------------------------------------------------
    @PostMapping("/appointments/{id}/check-in")
    public String checkInPatient(@PathVariable("id") Long appointmentId,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 RedirectAttributes redirectAttributes) {
        appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.CHECKED_IN, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Patient checked in successfully.");
        return "redirect:/receptionist/dashboard";
    }

    @PostMapping("/appointments/{id}/confirm")
    public String confirmAppointment(@PathVariable("id") Long appointmentId,
                                     @AuthenticationPrincipal CustomUserDetails user,
                                     RedirectAttributes redirectAttributes) {
        appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Appointment confirmed.");
        return "redirect:/receptionist/dashboard";
    }

    // -------------------------------------------------------------
    // Book Appointment on Behalf of Patient
    // -------------------------------------------------------------
    @GetMapping("/book")
    public String bookAppointmentPage(@RequestParam(value = "patientId", required = false) Long patientId,
                                      Model model) {
        model.addAttribute("bookingRequest", new AppointmentBookingRequest());
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("selectedPatientId", patientId);
        return "receptionist/book-appointment";
    }

    @PostMapping("/book")
    public String submitBooking(@Valid @ModelAttribute("bookingRequest") AppointmentBookingRequest request,
                                BindingResult result,
                                @AuthenticationPrincipal CustomUserDetails user,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "receptionist/book-appointment";
        }
        try {
            Appointment apt = appointmentService.bookAppointment(request, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Appointment booked successfully! Reference: " + apt.getAppointmentReference());
            return "redirect:/receptionist/dashboard";
        } catch (Exception e) {
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("errorMessage", e.getMessage());
            return "receptionist/book-appointment";
        }
    }

    // -------------------------------------------------------------
    // Reschedule and Cancel
    // -------------------------------------------------------------
    @GetMapping("/appointments/{id}/reschedule")
    public String rescheduleForm(@PathVariable("id") Long appointmentId, Model model) {
        Appointment apt = appointmentService.getAppointmentById(appointmentId);
        model.addAttribute("appointment", apt);
        model.addAttribute("rescheduleRequest", new AppointmentRescheduleRequest());
        return "receptionist/reschedule";
    }

    @PostMapping("/appointments/{id}/reschedule")
    public String submitReschedule(@PathVariable("id") Long appointmentId,
                                   @Valid @ModelAttribute("rescheduleRequest") AppointmentRescheduleRequest request,
                                   BindingResult result,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        request.setAppointmentId(appointmentId);
        try {
            Appointment updated = appointmentService.rescheduleAppointment(request, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Appointment " + updated.getAppointmentReference() + " rescheduled successfully.");
            return "redirect:/receptionist/dashboard";
        } catch (Exception e) {
            Appointment apt = appointmentService.getAppointmentById(appointmentId);
            model.addAttribute("appointment", apt);
            model.addAttribute("errorMessage", e.getMessage());
            return "receptionist/reschedule";
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable("id") Long appointmentId,
                                    @RequestParam(value = "reason", required = false, defaultValue = "Cancelled by front desk") String reason,
                                    @AuthenticationPrincipal CustomUserDetails user,
                                    RedirectAttributes redirectAttributes) {
        appointmentService.cancelAppointment(appointmentId, reason, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled and slot released.");
        return "redirect:/receptionist/dashboard";
    }

    // -------------------------------------------------------------
    // Waiting List
    // -------------------------------------------------------------
    @GetMapping("/waiting-list")
    public String waitingList(Model model) {
        model.addAttribute("queue", waitingListService.getActiveWaitingList());
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("waitingRequest", new WaitingListRequest());
        return "receptionist/waiting-list";
    }

    @PostMapping("/waiting-list/add")
    public String addToWaitingList(@Valid @ModelAttribute("waitingRequest") WaitingListRequest request,
                                   BindingResult result,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            waitingListService.addToWaitingList(request, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Added to waiting queue.");
        }
        return "redirect:/receptionist/waiting-list";
    }

    @PostMapping("/waiting-list/{id}/notify")
    public String notifyWaiting(@PathVariable("id") Long waitingId,
                                @AuthenticationPrincipal CustomUserDetails user,
                                RedirectAttributes redirectAttributes) {
        waitingListService.notifyNextInQueue(waitingId, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Notified patient of available slot.");
        return "redirect:/receptionist/waiting-list";
    }
}
