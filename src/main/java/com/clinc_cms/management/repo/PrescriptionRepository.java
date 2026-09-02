package com.clinc_cms.management.repo;

import com.clinc_cms.management.models.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByAppointmentId(Long appointmentId);
    Optional<Prescription> findById(String id);
}
