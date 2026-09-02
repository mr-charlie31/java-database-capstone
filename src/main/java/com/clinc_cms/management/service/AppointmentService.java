package com.clinc_cms.management.service;

import org.springframework.stereotype.Service;
import com.clinc_cms.management.dto.AppointmentDTO;
import com.clinc_cms.management.models.Appointment;
import com.clinc_cms.management.models.Doctor;
import com.clinc_cms.management.models.Patient;
import com.clinc_cms.management.models.Status;
import com.clinc_cms.management.repo.AppointmentRepository;
import com.clinc_cms.management.repo.DoctorRepository;
import com.clinc_cms.management.repo.PatientRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public Appointment book(AppointmentDTO dto) {
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
            .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
        Patient patient = patientRepository.findById(dto.getPatientId())
            .orElseThrow(() -> new NoSuchElementException("Patient not found"));

        boolean overlap = !appointmentRepository
            .findByDoctorIdAndAppointmentTimeBetween(
                doctor.getId(),
                dto.getAppointmentTime().minusMinutes(59),
                dto.getAppointmentTime().plusMinutes(59))
            .isEmpty();
        if (overlap) throw new IllegalStateException("Doctor already booked at this time");

        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setPatient(patient);
        appt.setAppointmentTime(dto.getAppointmentTime());
        appt.setStatus(Status.Scheduled);
        return appointmentRepository.save(appt);
    }

    public List<Appointment> getByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, LocalDateTime.now().with(LocalTime.MIN), LocalDateTime.now().with(LocalTime.MAX));
    }

    public Appointment update(Long id, AppointmentDTO dto) {
        Appointment appt = appointmentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Appointment not found"));
        if (dto.getAppointmentTime() != null) {
            boolean overlap = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(
                    appt.getDoctor().getId(),
                    dto.getAppointmentTime().minusMinutes(59),
                    dto.getAppointmentTime().plusMinutes(59))
                .stream().anyMatch(a -> !a.getId().equals(id));
            if (overlap) throw new IllegalStateException("Doctor already booked at this time");
            appt.setAppointmentTime(dto.getAppointmentTime());
        }
        return appointmentRepository.save(appt);
    }

    public void cancel(Long id) {
        Appointment appt = appointmentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Appointment not found"));
        appt.setStatus(Status.Cancelled);
        appointmentRepository.save(appt);
    }
}
