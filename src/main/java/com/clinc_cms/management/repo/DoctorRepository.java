package com.clinc_cms.management.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.clinc_cms.management.models.Doctor;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Doctor findByEmail(String email);
    List<Doctor> findByNameContainingIgnoreCase(String name);
    List<Doctor> findBySpecialityContainingIgnoreCase(String speciality);
    
} 
