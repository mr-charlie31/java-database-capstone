package com.clinc_cms.management.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinc_cms.management.dto.AppointmentDTO;
import com.clinc_cms.management.models.Appointment;
import com.clinc_cms.management.service.AppointmentService;


import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> book(@Valid @RequestBody AppointmentDTO dto) {
        return ResponseEntity.ok(appointmentService.book(dto));
    }

    @GetMapping("/patient/{patientId}")
    public List<Appointment> byPatient(@PathVariable Long patientId) {
        return appointmentService.getByPatientId(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> byDoctor(@PathVariable Long doctorId) {
        return appointmentService.getByDoctorId(doctorId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> update(@PathVariable Long id, @RequestBody AppointmentDTO dto) {
        return ResponseEntity.ok(appointmentService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
