package com.medislot.controller;

import com.medislot.config.CustomUserDetails;
import com.medislot.dto.PatientRegistrationDto;
import com.medislot.model.User;
import com.medislot.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final PatientService patientService;

    public AuthController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            User user = userDetails.getUser();
            return "redirect:" + user.getDashboardUrl();
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        if (userDetails != null) {
            return "redirect:" + userDetails.getUser().getDashboardUrl();
        }
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been successfully logged out.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:" + userDetails.getUser().getDashboardUrl();
        }
        model.addAttribute("patientForm", new PatientRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegistration(@Valid @ModelAttribute("patientForm") PatientRegistrationDto form,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            form.setCreateAccount(true);
            patientService.registerPatient(form, null);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! You can now sign in with your credentials.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage", "You do not possess the necessary administrative or role privileges to view this resource.");
        return "error/403";
    }
}
