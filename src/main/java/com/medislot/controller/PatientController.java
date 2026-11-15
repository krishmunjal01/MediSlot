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

import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final DoctorScheduleService scheduleService;
    private final PatientRecordService patientRecordService;
    private final WaitingListService waitingListService;

    public PatientController(PatientService patientService,
                             AppointmentService appointmentService,
                             DoctorService doctorService,
                             DepartmentService departmentService,
                             DoctorScheduleService scheduleService,
                             PatientRecordService patientRecordService,
                             WaitingListService waitingListService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.departmentService = departmentService;
        this.scheduleService = scheduleService;
        this.patientRecordService = patientRecordService;
        this.waitingListService = waitingListService;
    }

    private Patient getCurrentPatient(CustomUserDetails user) {
        return patientService.getPatientByUserId(user.getUserId());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Patient patient = getCurrentPatient(user);
        List<Appointment> all = appointmentService.getAppointmentsForPatient(patient.getPatientId());

        List<Appointment> upcoming = all.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED || a.getStatus() == AppointmentStatus.CONFIRMED)
                .toList();
        List<Appointment> past = all.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.CANCELLED)
                .toList();

        model.addAttribute("patient", patient);
        model.addAttribute("upcomingAppointments", upcoming);
        model.addAttribute("pastAppointments", past);
        return "patient/dashboard";
    }

    @GetMapping("/book")
    public String bookAppointmentPage(@RequestParam(value = "doctorId", required = false) Long doctorId,
                                      @AuthenticationPrincipal CustomUserDetails user,
                                      Model model) {
        Patient patient = getCurrentPatient(user);
        AppointmentBookingRequest request = new AppointmentBookingRequest();
        request.setPatientId(patient.getPatientId());
        if (doctorId != null) {
            request.setDoctorId(doctorId);
        }

        model.addAttribute("bookingRequest", request);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("patient", patient);
        return "patient/book";
    }

    @PostMapping("/book")
    public String confirmBooking(@Valid @ModelAttribute("bookingRequest") AppointmentBookingRequest request,
                                 BindingResult result,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Patient patient = getCurrentPatient(user);
        // Force session patient ID to prevent URL tampering
        request.setPatientId(patient.getPatientId());

        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("patient", patient);
            return "patient/book";
        }
        try {
            Appointment apt = appointmentService.bookAppointment(request, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Appointment confirmed! Your appointment reference is " + apt.getAppointmentReference());
            return "redirect:/patient/appointments/" + apt.getAppointmentId();
        } catch (Exception e) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("patient", patient);
            model.addAttribute("errorMessage", e.getMessage());
            return "patient/book";
        }
    }

    @GetMapping("/appointments")
    public String myAppointments(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Patient patient = getCurrentPatient(user);
        List<Appointment> list = appointmentService.getAppointmentsForPatient(patient.getPatientId());
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", list);
        return "patient/appointments";
    }

    @GetMapping("/appointments/{id}")
    public String appointmentDetails(@PathVariable("id") Long appointmentId,
                                     @AuthenticationPrincipal CustomUserDetails user,
                                     Model model) {
        Patient patient = getCurrentPatient(user);
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        // Security check: Verify patient owns this appointment
        if (!appointment.getPatientId().equals(patient.getPatientId())) {
            return "redirect:/access-denied";
        }

        Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId());
        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", patient);
        model.addAttribute("doctor", doctor);
        model.addAttribute("record", patientRecordService.getRecordByAppointmentId(appointmentId));
        return "patient/appointment-details";
    }

    @GetMapping("/appointments/{id}/reschedule")
    public String rescheduleForm(@PathVariable("id") Long appointmentId,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 Model model) {
        Patient patient = getCurrentPatient(user);
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        if (!appointment.getPatientId().equals(patient.getPatientId())) {
            return "redirect:/access-denied";
        }

        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        request.setAppointmentId(appointmentId);

        model.addAttribute("appointment", appointment);
        model.addAttribute("rescheduleRequest", request);
        return "patient/reschedule";
    }

    @PostMapping("/appointments/{id}/reschedule")
    public String submitReschedule(@PathVariable("id") Long appointmentId,
                                   @Valid @ModelAttribute("rescheduleRequest") AppointmentRescheduleRequest request,
                                   BindingResult result,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        Patient patient = getCurrentPatient(user);
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        if (!appointment.getPatientId().equals(patient.getPatientId())) {
            return "redirect:/access-denied";
        }

        request.setAppointmentId(appointmentId);
        try {
            Appointment updated = appointmentService.rescheduleAppointment(request, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Appointment rescheduled successfully to " + updated.getAppointmentDate() + " at " + updated.getStartTime());
            return "redirect:/patient/appointments/" + appointmentId;
        } catch (Exception e) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("errorMessage", e.getMessage());
            return "patient/reschedule";
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable("id") Long appointmentId,
                                    @RequestParam(value = "reason", required = false, defaultValue = "Cancelled by patient") String reason,
                                    @AuthenticationPrincipal CustomUserDetails user,
                                    RedirectAttributes redirectAttributes) {
        Patient patient = getCurrentPatient(user);
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        if (!appointment.getPatientId().equals(patient.getPatientId())) {
            return "redirect:/access-denied";
        }

        appointmentService.cancelAppointment(appointmentId, reason, user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Appointment has been cancelled and slot released.");
        return "redirect:/patient/appointments";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Patient patient = getCurrentPatient(user);
        PatientRegistrationDto dto = new PatientRegistrationDto();
        dto.setPatientId(patient.getPatientId());
        dto.setFullName(patient.getFullName());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setAddress(patient.getAddress());
        dto.setEmergencyContact(patient.getEmergencyContact());

        model.addAttribute("patient", patient);
        model.addAttribute("profileForm", dto);
        return "patient/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") PatientRegistrationDto form,
                                BindingResult result,
                                @AuthenticationPrincipal CustomUserDetails user,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        Patient patient = getCurrentPatient(user);
        form.setPatientId(patient.getPatientId());

        if (result.hasErrors()) {
            model.addAttribute("patient", patient);
            return "patient/profile";
        }
        try {
            patientService.updatePatient(form, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
            return "redirect:/patient/profile";
        } catch (Exception e) {
            model.addAttribute("patient", patient);
            model.addAttribute("errorMessage", e.getMessage());
            return "patient/profile";
        }
    }
}
