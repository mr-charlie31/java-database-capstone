package com.clinc_cms.management.repo;

import com.clinc_cms.management.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);

}
