package com.clinc_cms.management.mvc;

import com.clinc_cms.management.service.TokenService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class DashboardController {

    private final TokenService tokenService;

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        if (tokenService.validateToken(token, "admin")) {
            return "admin/adminDashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        if (tokenService.validateToken(token, "doctor")) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/patientDashboard/{token}")
    public String patientDashboard(@PathVariable String token) {
        if (tokenService.validateToken(token, "patient")) {
            return "patient/patientDashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
