package com.clinc_cms.management.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.clinc_cms.management.models.Prescription;
import com.clinc_cms.management.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<Prescription> create(@Valid @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.create(prescription));
    }

    @GetMapping("/appointment/{appointmentId}")
    public List<Prescription> byAppointment(@PathVariable Long appointmentId) {
        return prescriptionService.getByAppointmentId(appointmentId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getById(@PathVariable String id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prescription> update(@PathVariable String id, @Valid @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.update(id, prescription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
