package com.clinc_cms.management.repo;

import com.clinc_cms.management.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
    

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    

    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientId(Long patientId);


}
