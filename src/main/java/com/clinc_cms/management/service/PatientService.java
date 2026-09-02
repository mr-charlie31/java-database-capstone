package com.clinc_cms.management.service;

import com.clinc_cms.management.models.Patient;
import com.clinc_cms.management.repo.PatientRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    public Patient getById(Long id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Patient not found with id " + id));
    }

    public Patient register(Patient patient) {
        Patient existing = patientRepository.findByEmail(patient.getEmail());
        if (existing != null) {
            throw new IllegalStateException("An account with this email already exists");
        }
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        return patientRepository.save(patient);
    }

    public Patient update(Long id, Patient updated) {
        Patient patient = getById(id);
        patient.setName(updated.getName());
        patient.setPhone(updated.getPhone());
        patient.setAddress(updated.getAddress());
        return patientRepository.save(patient);
    }

    public void delete(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new NoSuchElementException("Patient not found with id " + id);
        }
        patientRepository.deleteById(id);
    }

    public Patient login(String email, String rawPassword) {
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null || !passwordEncoder.matches(rawPassword, patient.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return patient;
    }
}
