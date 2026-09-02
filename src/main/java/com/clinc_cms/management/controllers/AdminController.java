package com.clinc_cms.management.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinc_cms.management.dto.LoginDTO;
import com.clinc_cms.management.models.Admin;
import com.clinc_cms.management.service.AdminService;
import com.clinc_cms.management.service.TokenService;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        Admin admin = adminService.login(dto.getEmail(), dto.getPassword());
        String token = tokenService.generateToken(admin.getEmail(), "admin");
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping
    public ResponseEntity<Admin> create(@Valid @RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.create(admin));
    }
}
