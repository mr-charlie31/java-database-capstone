package com.clinc_cms.management.config;

import com.clinc_cms.management.models.Admin;
import com.clinc_cms.management.models.Doctor;
import com.clinc_cms.management.models.Patient;
import com.clinc_cms.management.repo.AdminRepository;
import com.clinc_cms.management.repo.DoctorRepository;
import com.clinc_cms.management.repo.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Initialize Admin
            if (adminRepository.findByEmail("admin@clinic.com") == null) {
                Admin admin = new Admin();
                admin.setUsername("admin1");
                admin.setEmail("admin@clinic.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                adminRepository.save(admin);
                System.out.println("✓ Admin user created: admin@clinic.com / Admin@123");
            }

            // Initialize Doctor
            if (doctorRepository.findByEmail("doctor@clinic.com") == null) {
                Doctor doctor = new Doctor();
                doctor.setName("Dr. John Smith");
                doctor.setEmail("doctor@clinic.com");
                doctor.setPassword(passwordEncoder.encode("Doctor@123"));
                doctor.setSpeciality("Cardiologist");
                doctor.setPhone("1234567890");
                doctorRepository.save(doctor);
                System.out.println("✓ Doctor user created: doctor@clinic.com / Doctor@123");
            }

            // Initialize Patient
            if (patientRepository.findByEmail("patient@clinic.com") == null) {
                Patient patient = new Patient();
                patient.setName("John Doe");
                patient.setEmail("patient@clinic.com");
                patient.setPassword(passwordEncoder.encode("Patient@123"));
                patient.setPhone("9876543210");
                patient.setAddress("123 Main St, City");
                patientRepository.save(patient);
                System.out.println("✓ Patient user created: patient@clinic.com / Patient@123");
            }
        };
    }
}
