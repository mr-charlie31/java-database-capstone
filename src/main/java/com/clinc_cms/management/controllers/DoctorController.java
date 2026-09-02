package com.clinc_cms.management.controllers;

import com.clinc_cms.management.dto.LoginDTO;
import com.clinc_cms.management.models.Doctor;
import com.clinc_cms.management.service.DoctorService;
import com.clinc_cms.management.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final TokenService tokenService;

    @GetMapping
    public List<Doctor> getAll() {
        return doctorService.getAll();
    }

    @GetMapping("/search")
    public List<Doctor> search(@RequestParam(required = false) String name,
                                @RequestParam(required = false) String specialty) {
        return doctorService.search(name, specialty);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Doctor> create(@Valid @RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.save(doctor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> update(@PathVariable Long id, @Valid @RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.update(id, doctor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        Doctor doctor = doctorService.login(dto.getEmail(), dto.getPassword());
        String token = tokenService.generateToken(doctor.getEmail(), "doctor");
        return ResponseEntity.ok(Map.of(
            "token", token,
            "doctorId", doctor.getId()
        ));
    }
}
