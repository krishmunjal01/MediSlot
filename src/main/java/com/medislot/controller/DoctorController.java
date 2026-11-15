package com.medislot.controller;

import com.medislot.config.CustomUserDetails;
import com.medislot.dto.ConsultationRecordDto;
import com.medislot.model.Appointment;
import com.medislot.model.Doctor;
import com.medislot.model.Patient;
import com.medislot.model.PatientRecord;
import com.medislot.model.enums.AppointmentStatus;
import com.medislot.service.AppointmentService;
import com.medislot.service.DoctorScheduleService;
import com.medislot.service.DoctorService;
import com.medislot.service.PatientRecordService;
import com.medislot.service.PatientService;
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
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final DoctorScheduleService scheduleService;
    private final PatientRecordService patientRecordService;
    private final PatientService patientService;

    public DoctorController(DoctorService doctorService,
                            AppointmentService appointmentService,
                            DoctorScheduleService scheduleService,
                            PatientRecordService patientRecordService,
                            PatientService patientService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.scheduleService = scheduleService;
        this.patientRecordService = patientRecordService;
        this.patientService = patientService;
    }

    private Doctor getCurrentDoctor(CustomUserDetails user) {
        return doctorService.getDoctorByUserId(user.getUserId());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Doctor doctor = getCurrentDoctor(user);
        List<Appointment> todayAppointments = appointmentService.getAppointmentsForDoctorAndDate(doctor.getDoctorId(), LocalDate.now());
        List<Appointment> allAppointments = appointmentService.getAppointmentsForDoctor(doctor.getDoctorId());

        long completedCount = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long pendingTodayCount = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.BOOKED || a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.CHECKED_IN).count();

        model.addAttribute("doctor", doctor);
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingTodayCount", pendingTodayCount);
        model.addAttribute("totalAssigned", allAppointments.size());
        return "doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal CustomUserDetails user,
                               @RequestParam(value = "date", required = false) String dateStr,
                               Model model) {
        Doctor doctor = getCurrentDoctor(user);
        List<Appointment> appointments;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            appointments = appointmentService.getAppointmentsForDoctorAndDate(doctor.getDoctorId(), LocalDate.parse(dateStr));
            model.addAttribute("selectedDate", dateStr);
        } else {
            appointments = appointmentService.getAppointmentsForDoctor(doctor.getDoctorId());
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        return "doctor/appointments";
    }

    @GetMapping("/appointments/{id}")
    public String appointmentDetails(@PathVariable("id") Long appointmentId,
                                     @AuthenticationPrincipal CustomUserDetails user,
                                     Model model) {
        Doctor doctor = getCurrentDoctor(user);
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        // Security check: Verify doctor ownership
        if (!appointment.getDoctorId().equals(doctor.getDoctorId())) {
            return "redirect:/access-denied";
        }

        Patient patient = patientService.getPatientById(appointment.getPatientId());
        PatientRecord existingRecord = patientRecordService.getRecordByAppointmentId(appointmentId);

        ConsultationRecordDto recordDto = new ConsultationRecordDto();
        recordDto.setAppointmentId(appointment.getAppointmentId());
        recordDto.setPatientId(patient.getPatientId());
        recordDto.setDoctorId(doctor.getDoctorId());

        if (existingRecord != null) {
            recordDto.setRecordId(existingRecord.getRecordId());
            recordDto.setSymptoms(existingRecord.getSymptoms());
            recordDto.setDiagnosis(existingRecord.getDiagnosis());
            recordDto.setPrescription(existingRecord.getPrescription());
            recordDto.setClinicalNotes(existingRecord.getClinicalNotes());
            recordDto.setFollowUpDate(existingRecord.getFollowUpDate());
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", patient);
        model.addAttribute("recordForm", recordDto);
        model.addAttribute("existingRecord", existingRecord);
        model.addAttribute("patientHistory", patientRecordService.getRecordsForPatient(patient.getPatientId()));
        return "doctor/appointment-details";
    }

    @PostMapping("/appointments/{id}/record")
    public String saveConsultationRecord(@PathVariable("id") Long appointmentId,
                                         @Valid @ModelAttribute("recordForm") ConsultationRecordDto form,
                                         BindingResult result,
                                         @AuthenticationPrincipal CustomUserDetails user,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        Doctor doctor = getCurrentDoctor(user);
        if (result.hasErrors()) {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            Patient patient = patientService.getPatientById(appointment.getPatientId());
            model.addAttribute("doctor", doctor);
            model.addAttribute("appointment", appointment);
            model.addAttribute("patient", patient);
            model.addAttribute("patientHistory", patientRecordService.getRecordsForPatient(patient.getPatientId()));
            return "doctor/appointment-details";
        }

        form.setAppointmentId(appointmentId);
        form.setDoctorId(doctor.getDoctorId());

        if (form.getRecordId() != null) {
            patientRecordService.updateRecord(form, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Consultation record updated successfully.");
        } else {
            patientRecordService.createRecord(form, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Consultation record saved and appointment completed.");
        }

        return "redirect:/doctor/appointments/" + appointmentId;
    }

    @PostMapping("/appointments/{id}/status")
    public String updateStatus(@PathVariable("id") Long appointmentId,
                               @RequestParam("status") String status,
                               @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes redirectAttributes) {
        Doctor doctor = getCurrentDoctor(user);
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        if (!appointment.getDoctorId().equals(doctor.getDoctorId())) {
            return "redirect:/access-denied";
        }

        appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.valueOf(status), user.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Appointment status updated to " + status + ".");
        return "redirect:/doctor/appointments/" + appointmentId;
    }

    @GetMapping("/availability")
    public String availability(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Doctor doctor = getCurrentDoctor(user);
        model.addAttribute("doctor", doctor);
        model.addAttribute("schedules", scheduleService.getSchedulesByDoctor(doctor.getDoctorId()));
        model.addAttribute("upcomingSlots", scheduleService.getAvailableSlots(doctor.getDoctorId(), LocalDate.now()));
        return "doctor/availability";
    }
}
