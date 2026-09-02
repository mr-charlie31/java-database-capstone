# Smart Clinic Management System — Full Build Guide

A complete, step-by-step guide to building the `java-database-capstone` project: Spring Boot backend (MVC + REST), MySQL (JPA) + MongoDB (Spring Data), and a separated Thymeleaf/HTML/CSS/JS frontend with client-side validation.

---

## Table of Contents
1. Architecture Summary
2. Project Setup
3. Folder Structure
4. Database Design (MySQL + MongoDB)
5. Entity Classes (JPA + MongoDB)
6. Repositories
7. DTOs
8. Service Layer
9. REST Controllers
10. MVC Controller (Login / Dashboards)
11. Security & JWT
12. `application.properties`
13. Frontend (separated HTML / CSS / JS)
14. Stored Procedures
15. Docker
16. GitHub Actions CI
17. `schema-architecture.md` & `schema-design.md` templates

---

## 1. Architecture Summary

The system uses a **three-tier architecture**:

- **Presentation layer** — Thymeleaf server-rendered pages (Admin/Doctor dashboards, login) for role-based screens, plus plain HTML/CSS/JS pages (patient-facing) that call REST APIs via `fetch`.
- **Application layer** — Spring Boot, split into MVC controllers (`@Controller`, return Thymeleaf views) and REST controllers (`@RestController`, return JSON). Both delegate to a shared **service layer**, which contains business logic and talks to two repository families.
- **Data layer** — MySQL (via Spring Data JPA) stores structured, relational data: `patients`, `doctors`, `appointments`, `admin`. MongoDB (via Spring Data MongoDB) stores flexible, document-shaped data: `prescriptions`.

Request flow: Browser → DispatcherServlet → Controller (MVC or REST) → Service → Repository (JPA or Mongo) → Database → back up the chain, either rendered as a Thymeleaf view or serialized as JSON.

---

## 2. Project Setup

1. Go to [https://start.spring.io](https://start.spring.io).
2. Configure:
   - Project: Maven
   - Language: Java 17
   - Group: `com.project`
   - Artifact: `back_end`
3. Dependencies:
   - Spring Web
   - Spring Data JPA
   - Spring Data MongoDB
   - Thymeleaf
   - MySQL Driver
   - Validation (Hibernate Validator)
   - Spring Security
   - Spring DevTools
   - Lombok (optional, cuts boilerplate)
4. Generate → unzip → open in your IDE.
5. Add JWT dependencies to `pom.xml` (jjwt):

```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.11.5</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.11.5</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.11.5</version>
  <scope>runtime</scope>
</dependency>
```

---

## 3. Folder Structure

```
java-database-capstone/
├── app/
│   ├── src/main/java/com/project/back_end/
│   │   ├── BackEndApplication.java
│   │   ├── models/            # @Entity + @Document classes
│   │   ├── repo/               # JPA + Mongo repositories
│   │   ├── dto/                 # request/response DTOs
│   │   ├── service/            # business logic
│   │   ├── controllers/        # @RestController classes
│   │   ├── mvc/                # @Controller (DashboardController)
│   │   └── security/           # JWT filter, config
│   └── src/main/resources/
│       ├── application.properties
│       ├── templates/
│       │   ├── admin/adminDashboard.html
│       │   ├── doctor/doctorDashboard.html
│       │   └── login.html
│       └── static/
│           ├── css/
│           │   ├── style.css
│           │   ├── adminDashboard.css
│           │   └── doctorDashboard.css
│           └── js/
│               ├── config/config.js
│               ├── services/
│               │   ├── doctorServices.js
│               │   ├── patientServices.js
│               │   ├── appointmentServices.js
│               │   └── prescriptionServices.js
│               ├── components/
│               │   ├── header.js
│               │   ├── footer.js
│               │   ├── modal.js
│               │   └── doctorCard.js
│               ├── pages/
│               │   ├── adminDashboard.js
│               │   ├── doctorDashboard.js
│               │   └── patientDashboard.js
│               └── index.js
├── schema-architecture.md
├── schema-design.md
└── .github/workflows/
    ├── frontend-lint.yml
    ├── backend-ci.yml
    └── docker-lint.yml
```

---

## 4. Database Design

### MySQL Database Design

```
### Table: admin
- id: INT, Primary Key, Auto Increment
- username: VARCHAR(50), NOT NULL, UNIQUE
- password: VARCHAR(255), NOT NULL   -- stored as BCrypt hash

### Table: doctors
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), NOT NULL
- specialty: VARCHAR(50), NOT NULL
- email: VARCHAR(100), NOT NULL, UNIQUE
- password: VARCHAR(255), NOT NULL
- phone: VARCHAR(10), NOT NULL

### Table: doctor_available_times   -- backs @ElementCollection
- doctor_id: INT, Foreign Key → doctors(id)
- available_time: VARCHAR(20)       -- e.g. "09:00-10:00"

### Table: patients
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), NOT NULL
- email: VARCHAR(100), NOT NULL, UNIQUE
- password: VARCHAR(255), NOT NULL
- phone: VARCHAR(10), NOT NULL
- address: VARCHAR(255), NOT NULL

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id), NOT NULL
- patient_id: INT, Foreign Key → patients(id), NOT NULL
- appointment_time: DATETIME, NOT NULL
- status: INT   -- 0 = Scheduled, 1 = Completed, 2 = Cancelled
```

Design notes:
- `appointments` uses `ON DELETE CASCADE` on both foreign keys so a deleted doctor/patient doesn't leave orphan rows (decide per your business rule — some clinics prefer `ON DELETE RESTRICT` to preserve history instead).
- `email` is `UNIQUE` on both `doctors` and `patients` since it doubles as the login identifier.
- Overlapping appointments for one doctor are prevented in the **service layer** (query existing appointments for that doctor/time window before insert), not at the DB constraint level, since that logic needs business context.

### MongoDB Collection Design

```
### Collection: prescriptions
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  },
  "tags": ["pain-relief", "otc"]
}
```

Design notes:
- Only `appointmentId` (not the full appointment object) is stored — MongoDB keeps a lightweight reference, MySQL remains the source of truth for the relational data.
- `pharmacy` is embedded since it's always read together with the prescription and never queried independently.
- `tags` as an array supports future filtering (e.g. "show all OTC prescriptions") without a schema migration.

---

## 5. Entity Classes

**Admin.java**
```java
package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Username cannot be null")
    private String username;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

**Doctor.java**
```java
package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Size(min = 3, max = 100)
    private String name;

    @NotNull @Size(min = 3, max = 50)
    private String specialty;

    @Email @NotNull
    private String email;

    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Pattern(regexp = "\\d{10}")
    private String phone;

    @ElementCollection
    private List<String> availableTimes;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public List<String> getAvailableTimes() { return availableTimes; }
    public void setAvailableTimes(List<String> availableTimes) { this.availableTimes = availableTimes; }
}
```

**Patient.java**
```java
package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Size(min = 3, max = 100)
    private String name;

    @Email @NotNull
    private String email;

    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Pattern(regexp = "\\d{10}")
    private String phone;

    @NotNull @Size(max = 255)
    private String address;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
```

**Appointment.java**
```java
package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @NotNull
    private Doctor doctor;

    @ManyToOne @NotNull
    private Patient patient;

    @Future
    private LocalDateTime appointmentTime;

    private int status; // 0 = Scheduled, 1 = Completed, 2 = Cancelled

    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime.plusHours(1);
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
```

**Prescription.java**
```java
package com.project.back_end.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

@Document(collection = "prescriptions")
public class Prescription {
    @Id
    private String id;

    @NotNull @Size(min = 3, max = 100)
    private String patientName;

    @NotNull
    private Long appointmentId;

    @NotNull @Size(min = 3, max = 100)
    private String medication;

    @Size(max = 50)
    private String dosage;

    @Size(max = 200)
    private String doctorNotes;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
}
```

---

## 6. Repositories

```java
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUsername(String username);
}

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByEmail(String email);
    List<Doctor> findByNameContainingIgnoreCase(String name);
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByEmail(String email);
}

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
        Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientId(Long patientId);
}

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByAppointmentId(Long appointmentId);
}
```

---

## 7. DTOs

```java
public class LoginDTO {
    @NotNull private String email;
    @NotNull private String password;
    // getters/setters
}

public class AppointmentDTO {
    private Long id;
    @NotNull private Long doctorId;
    @NotNull private Long patientId;
    @NotNull @Future private LocalDateTime appointmentTime;
    private int status;
    // getters/setters
}
```

---

## 8. Service Layer

```java
@Service
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PatientRepository patientRepository;

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
        appt.setStatus(0);
        return appointmentRepository.save(appt);
    }
}
```

Follow the same pattern for `DoctorService`, `PatientService`, `PrescriptionService`, `AdminService`, and a `TokenService` that generates/validates JWTs — full implementations below.

### `DoctorService.java`

```java
package com.project.back_end.service;

import com.project.back_end.models.Doctor;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DoctorService {

    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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
            return doctorRepository.findBySpecialtyIgnoreCase(specialty);
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
        doctor.setSpecialty(updated.getSpecialty());
        doctor.setPhone(updated.getPhone());
        doctor.setAvailableTimes(updated.getAvailableTimes());
        // email/password changes should go through dedicated endpoints, not a blanket update
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
```

### `PatientService.java`

```java
package com.project.back_end.service;

import com.project.back_end.models.Patient;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PatientService {

    @Autowired private PatientRepository patientRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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
```

### `PrescriptionService.java`

```java
package com.project.back_end.service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PrescriptionService {

    @Autowired private PrescriptionRepository prescriptionRepository;

    public Prescription create(Prescription prescription) {
        List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
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
```

### `AdminService.java`

```java
package com.project.back_end.service;

import com.project.back_end.models.Admin;
import com.project.back_end.repo.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired private AdminRepository adminRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public Admin login(String username, String rawPassword) {
        Admin admin = adminRepository.findByUsername(username);
        if (admin == null || !passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return admin;
    }

    public Admin create(Admin admin) {
        Admin existing = adminRepository.findByUsername(admin.getUsername());
        if (existing != null) {
            throw new IllegalStateException("An admin with this username already exists");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }
}
```

### `TokenService.java`

```java
package com.project.back_end.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {

    // Store this in application.properties / an env var in real deployments,
    // never hard-code a production secret.
    @Value("${jwt.secret:change-this-to-a-long-random-secret-in-properties}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}") // 1 hour default
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String subjectEmailOrUsername, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .setSubject(subjectEmailOrUsername)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /** Validates signature/expiry and, if roleRequired is non-null, checks the claim matches. */
    public boolean validateToken(String token, String roleRequired) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            if (roleRequired != null && !roleRequired.equalsIgnoreCase(String.valueOf(claims.get("role")))) {
                return false;
            }
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // covers ExpiredJwtException, MalformedJwtException, SignatureException, etc.
            return false;
        }
    }

    public String extractSubject(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public String extractRole(String token) {
        return (String) Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("role");
    }
}
```

Add the corresponding properties to `application.properties`:

```properties
jwt.secret=replace-with-a-long-random-string-at-least-32-chars
jwt.expiration-ms=3600000
```

Wire `TokenService` into a login endpoint, e.g. in `DoctorController`:

```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
    Doctor doctor = doctorService.login(dto.getEmail(), dto.getPassword());
    String token = tokenService.generateToken(doctor.getEmail(), "doctor");
    return ResponseEntity.ok(Map.of("token", token));
}
```

---

## 9. REST Controllers

```java
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    @Autowired private DoctorService doctorService;

    @GetMapping
    public List<Doctor> getAll() { return doctorService.getAll(); }

    @GetMapping("/search")
    public List<Doctor> search(@RequestParam(required=false) String name,
                                @RequestParam(required=false) String specialty) {
        return doctorService.search(name, specialty);
    }

    @PostMapping
    public ResponseEntity<Doctor> create(@Valid @RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.save(doctor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

Mirror this for `PatientController`, `AppointmentController`, `PrescriptionController`, and `AdminController`. Add a global handler:

### `PatientController.java`

```java
package com.project.back_end.controllers;

import com.project.back_end.dto.LoginDTO;
import com.project.back_end.models.Patient;
import com.project.back_end.service.PatientService;
import com.project.back_end.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired private PatientService patientService;
    @Autowired private TokenService tokenService;

    @PostMapping("/signup")
    public ResponseEntity<Patient> signup(@Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.register(patient));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        Patient patient = patientService.login(dto.getEmail(), dto.getPassword());
        String token = tokenService.generateToken(patient.getEmail(), "patient");
        return ResponseEntity.ok(Map.of(
            "token", token,
            "patientId", patient.getId()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping
    public List<Patient> getAll() {
        return patientService.getAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.update(id, patient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### `AppointmentController.java`

```java
package com.project.back_end.controllers;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired private AppointmentService appointmentService;

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
```

Add these two methods to `AppointmentService` alongside `book(...)`:

```java
public List<Appointment> getByPatientId(Long patientId) {
    return appointmentRepository.findByPatientId(patientId);
}

public List<Appointment> getByDoctorId(Long doctorId) {
    return appointmentRepository.findByDoctorId(doctorId);
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
    appt.setStatus(2); // Cancelled
    appointmentRepository.save(appt);
}
```

Add the matching finder to `AppointmentRepository`:
```java
List<Appointment> findByDoctorId(Long doctorId);
```

### `PrescriptionController.java`

```java
package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired private PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<Prescription> create(@Valid @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.create(prescription));
    }

    @GetMapping("/appointment/{appointmentId}")
    public List<Prescription> byAppointment(@PathVariable Long appointmentId) {
        return prescriptionService.getByAppointmentId(appointmentId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getById(@PathVariable String id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prescription> update(@PathVariable String id, @Valid @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.update(id, prescription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### `AdminController.java`

```java
package com.project.back_end.controllers;

import com.project.back_end.dto.LoginDTO;
import com.project.back_end.models.Admin;
import com.project.back_end.service.AdminService;
import com.project.back_end.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        // LoginDTO.email doubles as username here for a single shared DTO shape
        Admin admin = adminService.login(dto.getEmail(), dto.getPassword());
        String token = tokenService.generateToken(admin.getUsername(), "admin");
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping
    public ResponseEntity<Admin> create(@Valid @RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.create(admin));
    }
}
```

### `GlobalExceptionHandler.java` (expanded)

```java
package com.project.back_end.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Something went wrong. Please try again."));
    }
}
```

---

## 10. MVC Controller (Login / Dashboards)

```java
@Controller
public class DashboardController {

    @Autowired private TokenService tokenService;

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token, Model model) {
        if (tokenService.validateToken(token, "admin")) {
            return "admin/adminDashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token, Model model) {
        if (tokenService.validateToken(token, "doctor")) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/login";
    }
}
```

---

## 11. Security & JWT

```java
@Bean
public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
```

`TokenService` generates a signed JWT on login (`Jwts.builder().setSubject(email).claim("role", role)...signWith(key).compact()`) and validates it on each protected endpoint via a `OncePerRequestFilter` that reads the `Authorization: Bearer <token>` header, or the `{token}` path variable for the Thymeleaf dashboard routes.

---

## 12. `application.properties`

```properties
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/cms
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/cms_mongo

# Thymeleaf
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.cache=false
spring.thymeleaf.encoding=UTF-8

# Static resources
spring.web.resources.static-locations=classpath:/static/

# DevTools
spring.devtools.restart.enabled=true
```

---

## 13. Frontend — Separated HTML / CSS / JS

Keep frontend files fully separated by concern: **structure** (`templates/`, plain `.html`), **style** (`static/css/`), **behavior** (`static/js/`, split into `config`, `services`, `components`, `pages`). No inline `<style>` or `<script>` blocks in HTML.

### 13.1 `templates/login.html` (role selection — matches Image 1)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Hospital CMS</title>
  <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
  <header id="header"></header>

  <main class="role-select">
    <h1>Select Your <span class="highlight">Role</span></h1>
    <button id="adminBtn" class="role-btn">Admin</button>
    <button id="patientBtn" class="role-btn">Patient</button>
    <button id="doctorBtn" class="role-btn">Doctor</button>
  </main>

  <div id="modalRoot"></div>

  <footer id="footer"></footer>

  <script src="/js/config/config.js"></script>
  <script src="/js/components/header.js"></script>
  <script src="/js/components/footer.js"></script>
  <script src="/js/components/modal.js"></script>
  <script src="/js/index.js"></script>
</body>
</html>
```

### 13.2 `static/css/style.css` (shared/global styles)

```css
:root {
  --teal-dark: #0b3d3a;
  --teal-light: #d7f0ec;
  --accent-red: #c0392b;
  --white: #ffffff;
}

* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', sans-serif; }

body { background: var(--teal-light); min-height: 100vh; display: flex; flex-direction: column; }

header {
  background: var(--white);
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.role-select {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  text-align: center;
}

.role-select h1 { margin-bottom: 1.5rem; font-size: 2rem; }
.highlight { color: var(--accent-red); }

.role-btn {
  background: var(--teal-dark);
  color: var(--white);
  border: none;
  padding: 0.9rem 3rem;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  width: 260px;
  transition: transform 0.15s ease, opacity 0.15s ease;
}
.role-btn:hover { opacity: 0.9; transform: translateY(-1px); }

footer {
  background: var(--white);
  padding: 1.5rem 2rem;
  display: flex;
  justify-content: space-around;
  font-size: 0.85rem;
  color: #444;
}

.error-text { color: var(--accent-red); font-size: 0.8rem; margin-top: 0.25rem; }
input.invalid { border-color: var(--accent-red); }
```

### 13.3 `static/js/config/config.js`

```js
const API_BASE_URL = "http://localhost:8080/api";
```

### 13.4 `static/js/components/header.js`

```js
function renderHeader() {
  const header = document.getElementById("header");
  const role = localStorage.getItem("role");

  header.innerHTML = `
    <div class="brand">🏥 Hospital CMS</div>
    <nav>
      ${role
        ? `<a href="#" id="logoutLink">Logout</a>`
        : `<a href="/login">Login</a> <a href="/signup">Sign Up</a>`}
    </nav>
  `;

  const logoutLink = document.getElementById("logoutLink");
  if (logoutLink) {
    logoutLink.addEventListener("click", () => {
      localStorage.removeItem("token");
      localStorage.removeItem("role");
      window.location.href = "/login";
    });
  }
}
document.addEventListener("DOMContentLoaded", renderHeader);
```

### 13.5 `static/js/components/footer.js`

```js
function renderFooter() {
  document.getElementById("footer").innerHTML = `
    <div><strong>Company</strong><br><a href="#">About</a></div>
    <div><strong>Support</strong><br><a href="#">Account</a></div>
    <div><strong>Legals</strong><br><a href="#">Terms &amp; Conditions</a></div>
  `;
}
document.addEventListener("DOMContentLoaded", renderFooter);
```

### 13.6 `static/js/components/modal.js`

```js
function openModal(contentHtml) {
  const root = document.getElementById("modalRoot");
  root.innerHTML = `
    <div class="modal-overlay">
      <div class="modal-box">
        <button class="modal-close" id="modalCloseBtn">&times;</button>
        ${contentHtml}
      </div>
    </div>`;
  document.getElementById("modalCloseBtn").addEventListener("click", closeModal);
}
function closeModal() {
  document.getElementById("modalRoot").innerHTML = "";
}
```

### 13.7 `static/js/index.js` — role selection + login form validation

```js
document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("adminBtn").addEventListener("click", () => showLoginForm("admin"));
  document.getElementById("doctorBtn").addEventListener("click", () => showLoginForm("doctor"));
  document.getElementById("patientBtn").addEventListener("click", () => showLoginForm("patient"));
});

function showLoginForm(role) {
  openModal(`
    <h2>${role.charAt(0).toUpperCase() + role.slice(1)} Login</h2>
    <form id="loginForm" novalidate>
      <label>Email</label>
      <input type="email" id="email" required>
      <div class="error-text" id="emailError"></div>

      <label>Password</label>
      <input type="password" id="password" minlength="6" required>
      <div class="error-text" id="passwordError"></div>

      <button type="submit">Login</button>
    </form>
  `);

  document.getElementById("loginForm").addEventListener("submit", (e) => {
    e.preventDefault();
    if (validateLoginForm()) login(role);
  });
}

function validateLoginForm() {
  let valid = true;
  const email = document.getElementById("email");
  const password = document.getElementById("password");
  const emailError = document.getElementById("emailError");
  const passwordError = document.getElementById("passwordError");

  emailError.textContent = "";
  passwordError.textContent = "";
  email.classList.remove("invalid");
  password.classList.remove("invalid");

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailPattern.test(email.value)) {
    emailError.textContent = "Enter a valid email address.";
    email.classList.add("invalid");
    valid = false;
  }
  if (password.value.length < 6) {
    passwordError.textContent = "Password must be at least 6 characters.";
    password.classList.add("invalid");
    valid = false;
  }
  return valid;
}

async function login(role) {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  try {
    const res = await fetch(`${API_BASE_URL}/${role}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!res.ok) throw new Error("Invalid credentials");
    const data = await res.json();

    localStorage.setItem("token", data.token);
    localStorage.setItem("role", role);
    window.location.href = `/${role}Dashboard/${data.token}`;
  } catch (err) {
    document.getElementById("passwordError").textContent = err.message;
  }
}
```

### 13.8 `static/js/services/doctorServices.js`

```js
async function getAllDoctors() {
  const res = await fetch(`${API_BASE_URL}/doctors`);
  return res.ok ? res.json() : [];
}

async function searchDoctors(name = "", specialty = "") {
  const params = new URLSearchParams();
  if (name) params.append("name", name);
  if (specialty) params.append("specialty", specialty);
  const res = await fetch(`${API_BASE_URL}/doctors/search?${params}`);
  return res.ok ? res.json() : [];
}

async function addDoctor(doctor, token) {
  const res = await fetch(`${API_BASE_URL}/doctors`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify(doctor),
  });
  return res;
}

async function deleteDoctor(id, token) {
  return fetch(`${API_BASE_URL}/doctors/${id}`, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${token}` },
  });
}
```

### 13.9 `static/js/services/patientServices.js`

```js
async function getPatientAppointments(patientId, token) {
  const res = await fetch(`${API_BASE_URL}/appointments/patient/${patientId}`, {
    headers: { "Authorization": `Bearer ${token}` },
  });
  return res.ok ? res.json() : [];
}

async function getPatientPrescriptions(appointmentId, token) {
  const res = await fetch(`${API_BASE_URL}/prescriptions/appointment/${appointmentId}`, {
    headers: { "Authorization": `Bearer ${token}` },
  });
  return res.ok ? res.json() : [];
}
```

### 13.10 `static/js/components/doctorCard.js` (renders cards like Image 2)

```js
function renderDoctorCard(doctor, container) {
  const card = document.createElement("div");
  card.className = "doctor-card";
  card.innerHTML = `
    <h3>${doctor.name}</h3>
    <p>Specialization: ${doctor.specialty}</p>
    <p>Email: ${doctor.email}</p>
    <p>Available: ${doctor.availableTimes.join(", ")}</p>
    <button class="book-btn" data-id="${doctor.id}">Book Now</button>
  `;
  container.appendChild(card);
}
```

### 13.11 `static/js/pages/adminDashboard.js`

```js
document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const list = document.getElementById("doctorList");
  const searchInput = document.getElementById("searchBar");
  const timeFilter = document.getElementById("timeFilter");
  const specialtyFilter = document.getElementById("specialtyFilter");

  async function refresh() {
    list.innerHTML = "";
    const doctors = await searchDoctors(searchInput.value, specialtyFilter.value);
    const filtered = timeFilter.value
      ? doctors.filter(d => d.availableTimes.some(t => t.startsWith(timeFilter.value)))
      : doctors;
    filtered.forEach(d => renderDoctorCard(d, list));
  }

  searchInput.addEventListener("input", refresh);
  timeFilter.addEventListener("change", refresh);
  specialtyFilter.addEventListener("change", refresh);

  document.getElementById("addDoctorBtn").addEventListener("click", () => {
    openModal(`
      <h2>Add Doctor</h2>
      <form id="addDoctorForm" novalidate>
        <input type="text" id="docName" placeholder="Name" required minlength="3">
        <input type="text" id="docSpecialty" placeholder="Specialty" required>
        <input type="email" id="docEmail" placeholder="Email" required>
        <input type="password" id="docPassword" placeholder="Password" required minlength="6">
        <input type="tel" id="docPhone" placeholder="Phone (10 digits)" pattern="\\d{10}" required>
        <div class="error-text" id="addDoctorError"></div>
        <button type="submit">Save</button>
      </form>
    `);

    document.getElementById("addDoctorForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const phone = document.getElementById("docPhone").value;
      const errorBox = document.getElementById("addDoctorError");
      if (!/^\d{10}$/.test(phone)) {
        errorBox.textContent = "Phone must be exactly 10 digits.";
        return;
      }
      const doctor = {
        name: document.getElementById("docName").value,
        specialty: document.getElementById("docSpecialty").value,
        email: document.getElementById("docEmail").value,
        password: document.getElementById("docPassword").value,
        phone,
        availableTimes: [],
      };
      const res = await addDoctor(doctor, token);
      if (res.ok) { closeModal(); refresh(); }
      else errorBox.textContent = "Could not add doctor. Check the fields.";
    });
  });

  refresh();
});
```

### 13.12 `templates/admin/adminDashboard.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Hospital CMS - Admin</title>
  <link rel="stylesheet" th:href="@{/css/style.css}">
  <link rel="stylesheet" th:href="@{/css/adminDashboard.css}">
</head>
<body>
  <header id="header"></header>

  <main class="dashboard">
    <input type="text" id="searchBar" placeholder="Search Bar for custom output">
    <div class="filters">
      <select id="timeFilter">
        <option value="">Filter by Time</option>
        <option value="09">AM</option>
        <option value="14">PM</option>
      </select>
      <select id="specialtyFilter">
        <option value="">Filter by Specialty</option>
        <option value="Cardiologist">Cardiologist</option>
        <option value="Neurologist">Neurologist</option>
        <option value="Orthopedist">Orthopedist</option>
        <option value="Pediatrician">Pediatrician</option>
        <option value="Dermatologist">Dermatologist</option>
      </select>
    </div>
    <button id="addDoctorBtn" class="role-btn">Add Doctor</button>
    <div id="doctorList" class="doctor-grid"></div>
  </main>

  <div id="modalRoot"></div>
  <footer id="footer"></footer>

  <script src="/js/config/config.js"></script>
  <script src="/js/components/header.js"></script>
  <script src="/js/components/footer.js"></script>
  <script src="/js/components/modal.js"></script>
  <script src="/js/components/doctorCard.js"></script>
  <script src="/js/services/doctorServices.js"></script>
  <script src="/js/pages/adminDashboard.js"></script>
</body>
</html>
```

### 13.13 `static/css/adminDashboard.css`

```css
.dashboard { padding: 2rem; max-width: 1100px; margin: 0 auto; width: 100%; }

#searchBar {
  width: 100%;
  padding: 0.8rem 1rem;
  border-radius: 6px;
  border: 1px solid #ccc;
  margin-bottom: 1rem;
}

.filters { display: flex; gap: 1rem; margin-bottom: 1.5rem; }
.filters select { padding: 0.5rem 1rem; border-radius: 6px; border: 1px solid #ccc; }

.doctor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.2rem;
  margin-top: 1.5rem;
}

.doctor-card {
  background: var(--white);
  border-radius: 8px;
  padding: 1.2rem;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.doctor-card h3 { color: var(--teal-dark); margin-bottom: 0.4rem; }
.doctor-card p { font-size: 0.85rem; color: #555; margin-bottom: 0.2rem; }

.book-btn, .role-btn {
  background: var(--teal-dark);
  color: var(--white);
  border: none;
  padding: 0.6rem 1rem;
  border-radius: 6px;
  width: 100%;
  margin-top: 0.8rem;
  cursor: pointer;
}
```

### 13.14 `templates/doctor/doctorDashboard.html` (appointments + prescriptions view, matches Images 3–5)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Hospital CMS - Doctor</title>
  <link rel="stylesheet" th:href="@{/css/style.css}">
  <link rel="stylesheet" th:href="@{/css/doctorDashboard.css}">
</head>
<body>
  <header id="header"></header>

  <main class="dashboard">
    <input type="text" id="searchBar" placeholder="Search by patient name">
    <input type="date" id="dateFilter">
    <table id="appointmentTable">
      <thead>
        <tr><th>Date</th><th>Patient Name</th><th>Time</th><th>Prescription</th></tr>
      </thead>
      <tbody id="appointmentBody"></tbody>
    </table>
  </main>

  <div id="modalRoot"></div>
  <footer id="footer"></footer>

  <script src="/js/config/config.js"></script>
  <script src="/js/components/header.js"></script>
  <script src="/js/components/footer.js"></script>
  <script src="/js/components/modal.js"></script>
  <script src="/js/services/patientServices.js"></script>
  <script src="/js/pages/doctorDashboard.js"></script>
</body>
</html>
```

### 13.15 `static/css/doctorDashboard.css`

```css
.dashboard { padding: 2rem; max-width: 1000px; margin: 0 auto; width: 100%; }

table { width: 100%; border-collapse: collapse; margin-top: 1.5rem; background: var(--white); }
th, td { padding: 0.8rem 1rem; text-align: left; }
thead th { color: var(--teal-dark); border-bottom: 2px solid var(--teal-light); }
tbody tr:nth-child(even) { background: var(--teal-light); }

.rx-icon { cursor: pointer; width: 24px; }
```

### 13.16 `static/js/pages/doctorDashboard.js`

```js
document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const doctorId = localStorage.getItem("doctorId");
  const body = document.getElementById("appointmentBody");

  const appointments = await getPatientAppointments(doctorId, token);

  appointments.forEach(appt => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${new Date(appt.appointmentTime).toLocaleDateString()}</td>
      <td>${appt.patientName}</td>
      <td>${new Date(appt.appointmentTime).toLocaleTimeString()}</td>
      <td><img src="/images/rx-icon.png" class="rx-icon" data-appt="${appt.id}"></td>
    `;
    body.appendChild(row);
  });

  body.addEventListener("click", async (e) => {
    if (!e.target.classList.contains("rx-icon")) return;
    const apptId = e.target.dataset.appt;
    const rx = await getPatientPrescriptions(apptId, token);
    if (!rx.length) return openModal("<p>No prescription found.</p>");
    const p = rx[0];
    openModal(`
      <h2>View <span class="highlight">Prescription</span></h2>
      <label>Patient Name</label><input value="${p.patientName}" disabled>
      <label>Medicine Names</label><input value="${p.medication}" disabled>
      <label>Dosage Instructions</label><textarea disabled>${p.dosage}</textarea>
      <label>Additional Notes</label><textarea disabled>${p.doctorNotes || "NA"}</textarea>
      <button id="cancelRx">Cancel</button>
    `);
    document.getElementById("cancelRx").addEventListener("click", closeModal);
  });
});
```

---

## 14. Stored Procedures

```sql
DELIMITER //
CREATE PROCEDURE daily_appointments_by_doctor(IN target_date DATE)
BEGIN
  SELECT d.name AS doctor_name, COUNT(*) AS appointment_count
  FROM appointments a
  JOIN doctors d ON a.doctor_id = d.id
  WHERE DATE(a.appointment_time) = target_date
  GROUP BY d.name;
END //

CREATE PROCEDURE top_doctor_by_month(IN target_month INT, IN target_year INT)
BEGIN
  SELECT d.name, COUNT(*) AS patient_count
  FROM appointments a
  JOIN doctors d ON a.doctor_id = d.id
  WHERE MONTH(a.appointment_time) = target_month AND YEAR(a.appointment_time) = target_year
  GROUP BY d.name
  ORDER BY patient_count DESC
  LIMIT 1;
END //

CREATE PROCEDURE top_doctor_by_year(IN target_year INT)
BEGIN
  SELECT d.name, COUNT(*) AS patient_count
  FROM appointments a
  JOIN doctors d ON a.doctor_id = d.id
  WHERE YEAR(a.appointment_time) = target_year
  GROUP BY d.name
  ORDER BY patient_count DESC
  LIMIT 1;
END //
DELIMITER ;
```

---

## 15. Docker

`Dockerfile` (multi-stage):

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build & run:
```bash
docker build -t smart-clinic-backend .
docker run -p 8080:8080 smart-clinic-backend
```

---

## 16. GitHub Actions CI

`.github/workflows/backend-ci.yml`

```yaml
name: Backend CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Compile with Maven
        run: mvn -B compile --file app/pom.xml
      - name: Run Checkstyle
        run: mvn checkstyle:check --file app/pom.xml
```

`.github/workflows/frontend-lint.yml`

```yaml
name: Frontend Lint
on: [push, pull_request]
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v4
        with:
          node-version: '18'
      - run: npx htmlhint "app/src/main/resources/templates/**/*.html"
      - run: npx stylelint "app/src/main/resources/static/css/**/*.css"
      - run: npx eslint "app/src/main/resources/static/js/**/*.js"
```

`.github/workflows/docker-lint.yml`

```yaml
name: Dockerfile Lint
on: [push, pull_request]
jobs:
  hadolint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: hadolint/hadolint-action@v3.1.0
        with:
          dockerfile: Dockerfile
```

---

## 17. `schema-architecture.md` & `schema-design.md` — ready to paste

**`schema-architecture.md`**
```markdown
## Architecture Summary
The Smart Clinic Management System follows a three-tier architecture. The
presentation tier is a mix of Thymeleaf-rendered pages (Admin/Doctor
dashboards, served by MVC controllers) and static HTML/JS pages for
patients that call REST endpoints. The application tier is a Spring Boot
service that separates MVC controllers, REST controllers, and a shared
service layer implementing business rules such as booking conflict
checks. The data tier splits storage by data shape: structured entities
(patients, doctors, admin, appointments) live in MySQL via Spring Data
JPA, while flexible, document-style data (prescriptions) lives in
MongoDB via Spring Data MongoDB.

## Numbered Flow
1. User opens the app and picks a role (Admin, Doctor, or Patient) on the landing page.
2. The browser submits login credentials to a REST login endpoint.
3. The service layer validates credentials against MySQL and issues a signed JWT.
4. The browser redirects to `/{role}Dashboard/{token}`, handled by `DashboardController` (MVC).
5. `DashboardController` validates the token via `TokenService` and returns the correct Thymeleaf view.
6. Dashboard JS calls REST endpoints (e.g. `/api/doctors`, `/api/appointments`) with the JWT attached.
7. REST controllers delegate to service classes, which apply validation and business rules.
8. Services call JPA repositories for relational data or Mongo repositories for prescriptions.
9. Repositories query MySQL or MongoDB and return domain objects/documents.
10. Services map results to DTOs; controllers serialize them to JSON.
11. Frontend JS renders the JSON into the DOM (tables, cards, modals).
```

**`schema-design.md`** — use the MySQL and MongoDB sections from **Section 4** of this guide directly; they're already in the exact format requested by the assignment.

---

## 18. User Stories (Module 1 deliverable)

Create these as GitHub Issues (label `user-story`) in `java-database-capstone`, or paste into a `user-stories.md` file.

**Admin**
1. As an admin, I want to log in with my username and password so I can securely manage the platform.
2. As an admin, I want to add a new doctor's profile so they can be booked by patients.
3. As an admin, I want to search and filter the doctor list by name, specialty, and time so I can quickly find doctors.
4. As an admin, I want to delete a doctor's profile so I can remove staff who no longer work at the clinic.
5. As an admin, I want to run monthly/yearly usage reports so I can track appointment volume per doctor.

**Doctor**
1. As a doctor, I want to log in so I can access my personal dashboard.
2. As a doctor, I want to view all my upcoming appointments so I can plan my day.
3. As a doctor, I want to search my appointments by patient name and filter by date so I can find a specific visit quickly.
4. As a doctor, I want to view a patient's prescription history so I have full context before a visit.
5. As a doctor, I want to set my available time slots so patients can only book when I'm free.

**Patient**
1. As a patient, I want to sign up with my name, email, and address so I can create an account.
2. As a patient, I want to browse doctors by specialty so I can find the right one for my condition.
3. As a patient, I want to book an appointment in an available time slot so I can be seen by a doctor.
4. As a patient, I want to update or cancel an existing appointment so I can adjust my schedule.
5. As a patient, I want to view my past prescriptions so I can review my treatment history.

---

## 19. Sample Data (Module 3 deliverable)

```sql
USE cms;

INSERT INTO admin (username, password) VALUES
('admin', '$2a$10$examplebcrypthashvalueXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX');

INSERT INTO doctors (name, specialty, email, password, phone) VALUES
('Dr. Emily Adams', 'Cardiologist', 'dr.adams@example.com', '$2a$10$hash1', '3125550101'),
('Dr. Mark Johnson', 'Neurologist', 'dr.johnson@example.com', '$2a$10$hash2', '3125550102'),
('Dr. Sarah Lee', 'Orthopedist', 'dr.lee@example.com', '$2a$10$hash3', '3125550103'),
('Dr. Tom Wilson', 'Pediatrician', 'dr.wilson@example.com', '$2a$10$hash4', '3125550104'),
('Dr. Alice Brown', 'Dermatologist', 'dr.brown@example.com', '$2a$10$hash5', '3125550105'),
('Dr. Taylor Grant', 'Cardiologist', 'dr.taylor@example.com', '$2a$10$hash6', '3125550106');

INSERT INTO doctor_available_times (doctor_id, available_time) VALUES
(1, '09:00-10:00'), (1, '10:00-11:00'), (1, '11:00-12:00'), (1, '14:00-15:00'),
(2, '10:00-11:00'), (2, '11:00-12:00'), (2, '14:00-15:00'), (2, '15:00-16:00');

INSERT INTO patients (name, email, password, phone, address) VALUES
('John Smith', 'john.smith@example.com', '$2a$10$hashp1', '3125550201', '123 Main St, Chicago, IL'),
('Jane Doe', 'jane.doe@example.com', '$2a$10$hashp2', '3125550202', '456 Oak Ave, Chicago, IL');

INSERT INTO appointments (doctor_id, patient_id, appointment_time, status) VALUES
(1, 1, '2026-09-15 09:00:00', 0),
(2, 1, '2026-09-16 11:00:00', 0),
(3, 2, '2026-09-17 14:00:00', 1);
```

MongoDB sample insert (`mongosh` or via a Spring `CommandLineRunner`):

```js
db.prescriptions.insertMany([
  {
    patientName: "John Smith",
    appointmentId: 1,
    medication: "Vitamin C tablets",
    dosage: "Twice a day",
    doctorNotes: "NA",
    refillCount: 0,
    tags: ["otc", "supplement"]
  },
  {
    patientName: "Jane Doe",
    appointmentId: 3,
    medication: "Amoxicillin",
    dosage: "500mg, 3 times a day",
    doctorNotes: "Complete the full course.",
    refillCount: 1,
    pharmacy: { name: "Walgreens SF", location: "Market Street" },
    tags: ["antibiotic"]
  }
]);
```

---

## 20. Remaining Frontend Service Files

**`static/js/services/appointmentServices.js`**
```js
async function bookAppointment(appointmentDto, token) {
  const res = await fetch(`${API_BASE_URL}/appointments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify(appointmentDto),
  });
  return res;
}

async function updateAppointment(id, appointmentDto, token) {
  const res = await fetch(`${API_BASE_URL}/appointments/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify(appointmentDto),
  });
  return res;
}

async function cancelAppointment(id, token) {
  return fetch(`${API_BASE_URL}/appointments/${id}`, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${token}` },
  });
}

async function getAppointmentsByPatient(patientId, token) {
  const res = await fetch(`${API_BASE_URL}/appointments/patient/${patientId}`, {
    headers: { "Authorization": `Bearer ${token}` },
  });
  return res.ok ? res.json() : [];
}
```

**`static/js/services/prescriptionServices.js`**
```js
async function getPrescriptionsByAppointment(appointmentId, token) {
  const res = await fetch(`${API_BASE_URL}/prescriptions/appointment/${appointmentId}`, {
    headers: { "Authorization": `Bearer ${token}` },
  });
  return res.ok ? res.json() : [];
}

async function createPrescription(prescription, token) {
  const res = await fetch(`${API_BASE_URL}/prescriptions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify(prescription),
  });
  return res;
}
```

---

## 21. Patient-Facing Pages (browse doctors, book & update appointments — Images 2 & 3)

### `templates/patient/patientDashboard.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Hospital CMS - Find a Doctor</title>
  <link rel="stylesheet" th:href="@{/css/style.css}">
  <link rel="stylesheet" th:href="@{/css/adminDashboard.css}">
</head>
<body>
  <header id="header"></header>

  <main class="dashboard">
    <input type="text" id="searchBar" placeholder="Search Bar for custom output">
    <div class="filters">
      <select id="timeFilter">
        <option value="">Filter by Time</option>
        <option value="09">AM</option>
        <option value="14">PM</option>
      </select>
      <select id="specialtyFilter">
        <option value="">Filter by Specialty</option>
        <option value="Cardiologist">Cardiologist</option>
        <option value="Neurologist">Neurologist</option>
        <option value="Orthopedist">Orthopedist</option>
        <option value="Pediatrician">Pediatrician</option>
        <option value="Dermatologist">Dermatologist</option>
      </select>
    </div>
    <div id="doctorList" class="doctor-grid"></div>
  </main>

  <div id="modalRoot"></div>
  <footer id="footer"></footer>

  <script src="/js/config/config.js"></script>
  <script src="/js/components/header.js"></script>
  <script src="/js/components/footer.js"></script>
  <script src="/js/components/modal.js"></script>
  <script src="/js/components/doctorCard.js"></script>
  <script src="/js/services/doctorServices.js"></script>
  <script src="/js/services/appointmentServices.js"></script>
  <script src="/js/pages/patientDashboard.js"></script>
</body>
</html>
```

### `static/js/pages/patientDashboard.js` — browse + book, with validation

```js
document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const patientId = localStorage.getItem("patientId");
  const list = document.getElementById("doctorList");
  const searchInput = document.getElementById("searchBar");
  const timeFilter = document.getElementById("timeFilter");
  const specialtyFilter = document.getElementById("specialtyFilter");

  async function refresh() {
    list.innerHTML = "";
    const doctors = await searchDoctors(searchInput.value, specialtyFilter.value);
    const filtered = timeFilter.value
      ? doctors.filter(d => d.availableTimes.some(t => t.startsWith(timeFilter.value)))
      : doctors;
    filtered.forEach(d => renderDoctorCard(d, list));
  }

  searchInput.addEventListener("input", refresh);
  timeFilter.addEventListener("change", refresh);
  specialtyFilter.addEventListener("change", refresh);

  list.addEventListener("click", (e) => {
    if (!e.target.classList.contains("book-btn")) return;
    const doctorId = e.target.dataset.id;
    openBookingForm(doctorId);
  });

  function openBookingForm(doctorId) {
    const today = new Date().toISOString().split("T")[0];
    openModal(`
      <h2>Book <span class="highlight">Appointment</span></h2>
      <form id="bookForm" novalidate>
        <label>Date</label>
        <input type="date" id="apptDate" min="${today}" required>
        <label>Time</label>
        <select id="apptTime" required>
          <option value="">Select a time</option>
          <option value="09:00">09:00-10:00</option>
          <option value="10:00">10:00-11:00</option>
          <option value="11:00">11:00-12:00</option>
          <option value="14:00">14:00-15:00</option>
        </select>
        <div class="error-text" id="bookError"></div>
        <button type="submit">Book Appointment</button>
      </form>
    `);

    document.getElementById("bookForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const date = document.getElementById("apptDate").value;
      const time = document.getElementById("apptTime").value;
      const errorBox = document.getElementById("bookError");

      if (!date || !time) {
        errorBox.textContent = "Please select both a date and a time.";
        return;
      }
      const appointmentTime = `${date}T${time}:00`;
      if (new Date(appointmentTime) <= new Date()) {
        errorBox.textContent = "Appointment time must be in the future.";
        return;
      }

      const dto = { doctorId, patientId, appointmentTime, status: 0 };
      const res = await bookAppointment(dto, token);
      if (res.ok) {
        closeModal();
        alert("Appointment booked successfully.");
      } else {
        const msg = await res.text();
        errorBox.textContent = msg || "Could not book this slot. Try another time.";
      }
    });
  }

  refresh();
});
```

### `templates/patient/updateAppointment.html` (matches Image 3)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Update Appointment</title>
  <link rel="stylesheet" th:href="@{/css/style.css}">
  <link rel="stylesheet" th:href="@{/css/updateAppointment.css}">
</head>
<body>
  <header id="header"></header>

  <main class="update-form">
    <h1>Update <span class="highlight">Appointment</span></h1>
    <form id="updateForm" novalidate>
      <label>Patient Name</label>
      <input type="text" id="patientName" disabled>

      <label>Doctor Name</label>
      <input type="text" id="doctorName" disabled>

      <label>Date</label>
      <input type="date" id="apptDate" required>

      <label>Time</label>
      <select id="apptTime" required>
        <option value="09:00">09:00-10:00</option>
        <option value="10:00">10:00-11:00</option>
        <option value="11:00">11:00-12:00</option>
        <option value="14:00">14:00-15:00</option>
      </select>

      <div class="error-text" id="updateError"></div>
      <button type="submit" class="role-btn">Update Appointment</button>
      <button type="button" id="cancelBtn" class="role-btn secondary">Cancel</button>
    </form>
  </main>

  <script src="/js/config/config.js"></script>
  <script src="/js/components/header.js"></script>
  <script src="/js/services/appointmentServices.js"></script>
  <script src="/js/pages/updateAppointment.js"></script>
</body>
</html>
```

### `static/css/updateAppointment.css`

```css
.update-form {
  max-width: 480px;
  margin: 3rem auto;
  background: var(--white);
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}
.update-form h1 { text-align: center; margin-bottom: 1rem; }
.update-form input, .update-form select {
  padding: 0.7rem;
  border: 1px solid #ccc;
  border-radius: 6px;
}
.role-btn.secondary { background: var(--white); color: var(--teal-dark); border: 1px solid var(--teal-dark); }
```

### `static/js/pages/updateAppointment.js`

```js
document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token");
  const params = new URLSearchParams(window.location.search);
  const appointmentId = params.get("id");

  document.getElementById("cancelBtn").addEventListener("click", () => {
    window.location.href = "/patientDashboard";
  });

  document.getElementById("updateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const date = document.getElementById("apptDate").value;
    const time = document.getElementById("apptTime").value;
    const errorBox = document.getElementById("updateError");

    if (!date || !time) {
      errorBox.textContent = "Please select both a date and a time.";
      return;
    }
    const appointmentTime = `${date}T${time}:00`;
    if (new Date(appointmentTime) <= new Date()) {
      errorBox.textContent = "Appointment time must be in the future.";
      return;
    }

    const res = await updateAppointment(appointmentId, { appointmentTime }, token);
    if (res.ok) {
      alert("Appointment updated.");
      window.location.href = "/patientDashboard";
    } else {
      errorBox.textContent = "Could not update — the slot may be taken.";
    }
  });
});
```

---

## 22. Module 6 — Final Submission & Reflection Template

Create `REFLECTION.md` at the repo root:

```markdown
# Capstone Reflection — Smart Clinic Management System

## What I built
A three-tier Spring Boot application (MVC + REST) with MySQL for
relational data (patients, doctors, admin, appointments) and MongoDB
for flexible prescription documents, plus a role-based frontend
(Admin, Doctor, Patient dashboards) secured with JWT.

## Links (fill in with your repo's actual paths)
- Architecture doc: `schema-architecture.md`
- Schema design: `schema-design.md`
- User stories: `user-stories.md` (or GitHub Issues, label `user-story`)
- Entities: `app/src/main/java/com/project/back_end/models/`
- DoctorController: `.../controllers/DoctorController.java`
- PrescriptionController: `.../controllers/PrescriptionController.java`
- PatientRepository: `.../repo/PatientRepository.java`
- AppointmentService: `.../service/AppointmentService.java`
- TokenService: `.../service/TokenService.java`
- DoctorService: `.../service/DoctorService.java`
- Dockerfile: `Dockerfile`
- CI workflow (backend compile): `.github/workflows/backend-ci.yml`

## Challenges & decisions
- Chose to check appointment overlap in the service layer rather than
  a DB constraint, since it needs a time-window comparison.
- Split MySQL vs. MongoDB by data shape: relational/queried-by-key data
  in MySQL, free-form/nested data (prescriptions) in MongoDB.

## What I'd improve with more time
- Add refresh tokens instead of a single long-lived JWT.
- Add integration tests for the REST controllers.
- Add pagination to the doctor search endpoint.
```

Final checklist before submitting Module 6:
- [ ] All labs' files committed with the exact commit messages specified in each assignment.
- [ ] `schema-architecture.md` and `schema-design.md` present at repo root.
- [ ] All five required controller/service/repository links (Section 9's deliverables) resolve publicly.
- [ ] GitHub Actions workflow badge/link for the Maven compile job.
- [ ] `Dockerfile` builds and runs locally (`docker build` + `docker run` succeed).
- [ ] `REFLECTION.md` added and pushed.

---

## Execution Order Checklist

1. Scaffold project (Section 2) → 2. Configure `application.properties` (Section 12) → 3. Write entities (Section 5) → 4. Write repositories (Section 6) → 5. Write DTOs (Section 7) → 6. Write services (Section 8) → 7. Write REST controllers (Section 9) → 8. Write `DashboardController` + JWT (Sections 10–11) → 9. Build frontend files exactly as separated in Section 13 → 10. Add stored procedures + sample data (Section 14) → 11. Dockerize (Section 15) → 12. Add CI workflows (Section 16) → 13. Write `schema-architecture.md` / `schema-design.md` and push with the required commit messages.
