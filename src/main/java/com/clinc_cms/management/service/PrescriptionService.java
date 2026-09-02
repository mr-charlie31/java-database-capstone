package com.clinc_cms.management.service;

import org.springframework.stereotype.Service;
import com.clinc_cms.management.models.Prescription;
import com.clinc_cms.management.repo.PrescriptionRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public Prescription create(Prescription prescription) {
        List<Prescription> existing = prescriptionRepository.findByAppointmentId(Long.parseLong(prescription.getAppointmentId()));
        if (!existing.isEmpty()) {
            throw new IllegalStateException("A prescription already exists for this appointment");
        }
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getByAppointmentId(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }

    public Prescription getById(String id) {
        return prescriptionRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Prescription not found with id " + id));
    }

    public Prescription update(String id, Prescription updated) {
        Prescription prescription = getById(id);
        prescription.setMedication(updated.getMedication());
        prescription.setDosage(updated.getDosage());
        prescription.setDoctorNotes(updated.getDoctorNotes());
        return prescriptionRepository.save(prescription);
    }

    public void delete(String id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new NoSuchElementException("Prescription not found with id " + id);
        }
        prescriptionRepository.deleteById(id);
    }
}
