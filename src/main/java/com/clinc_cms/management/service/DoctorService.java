package com.clinc_cms.management.service;
import com.clinc_cms.management.models.Doctor;
import com.clinc_cms.management.repo.DoctorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    public Doctor getById(Long id) {
        return doctorRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Doctor not found with id " + id));
    }

    public List<Doctor> search(String name, String specialty) {
        if (name != null && !name.isBlank()) {
            return doctorRepository.findByNameContainingIgnoreCase(name);
        }
        if (specialty != null && !specialty.isBlank()) {
            return doctorRepository.findBySpecialityContainingIgnoreCase(specialty);
        }
        return doctorRepository.findAll();
    }

    public Doctor save(Doctor doctor) {
        Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
        if (existing != null) {
            throw new IllegalStateException("A doctor with this email already exists");
        }
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        return doctorRepository.save(doctor);
    }

    public Doctor update(Long id, Doctor updated) {
        Doctor doctor = getById(id);
        doctor.setName(updated.getName());
        doctor.setSpeciality(updated.getSpeciality());
        doctor.setPhone(updated.getPhone());
        doctor.setAvailableTimes(updated.getAvailableTimes());
        return doctorRepository.save(doctor);
    }

    public void delete(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new NoSuchElementException("Doctor not found with id " + id);
        }
        doctorRepository.deleteById(id);
    }

    public Doctor login(String email, String rawPassword) {
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null || !passwordEncoder.matches(rawPassword, doctor.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return doctor;
    }
}
