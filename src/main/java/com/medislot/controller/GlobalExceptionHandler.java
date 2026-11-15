package com.medislot.controller;

import com.medislot.exception.DuplicateBookingException;
import com.medislot.exception.InvalidAppointmentStateException;
import com.medislot.exception.ResourceNotFoundException;
import com.medislot.exception.SlotUnavailableException;
import com.medislot.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateBookingException.class)
    public String handleDuplicateBooking(DuplicateBookingException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Handled DuplicateBookingException on {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(SlotUnavailableException.class)
    public String handleSlotUnavailable(SlotUnavailableException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Handled SlotUnavailableException: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(InvalidAppointmentStateException.class)
    public String handleInvalidState(InvalidAppointmentStateException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Handled InvalidAppointmentStateException: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidation(ValidationException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Handled ValidationException: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Handled ResourceNotFoundException: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Record Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model, HttpServletRequest request) {
        log.error("Unhandled system exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorTitle", "An Unexpected Error Occurred");
        model.addAttribute("errorMessage", "The operations team has been notified. Please try again in a few moments.");
        return "error/500";
    }
}