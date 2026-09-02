package com.clinc_cms.management.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.clinc_cms.management.dto.LoginDTO;
import com.clinc_cms.management.models.Patient;
import com.clinc_cms.management.service.PatientService;
import com.clinc_cms.management.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;
    private final TokenService tokenService;

    @PostMapping("/signup")
    public ResponseEntity<Patient> signup(@Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.register(patient));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        Patient patient = patientService.login(dto.getEmail(), dto.getPassword());
        String token = tokenService.generateToken(patient.getEmail(), "patient");
        return ResponseEntity.ok(Map.of(
            "token", token,
            "patientId", patient.getId()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping
    public List<Patient> getAll() {
        return patientService.getAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.update(id, patient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
