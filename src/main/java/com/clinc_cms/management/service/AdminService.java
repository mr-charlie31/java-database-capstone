package com.clinc_cms.management.service;

import com.clinc_cms.management.models.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.clinc_cms.management.repo.AdminRepository;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin login(String email, String rawPassword) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null || !passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return admin;
    }

    public Admin create(Admin admin) {
        Admin existing = adminRepository.findByUsername(admin.getUsername());
        if (existing != null) {
            throw new IllegalStateException("An admin with this username already exists");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }
}
