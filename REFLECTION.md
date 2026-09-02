# Capstone Reflection — Smart Clinic Management System

## What I built
A three-tier Spring Boot application (MVC + REST) with MySQL for
relational data (patients, doctors, admin, appointments) and MongoDB
for flexible prescription documents, plus a role-based frontend
(Admin, Doctor, Patient dashboards) secured with JWT.

## Links (fill in with your repo's actual paths)
- Architecture doc: `schema-architecture.md`
- Schema design: `schema-design.md`
- User stories: `user-stories.md`
- Entities: `app/src/main/java/com/project/back_end/models/`
- DoctorController: `.../controllers/DoctorController.java`
- PrescriptionController: `.../controllers/PrescriptionController.java`
- PatientRepository: `.../repo/PatientRepository.java`
- AppointmentService: `.../service/AppointmentService.java`
- TokenService: `.../service/TokenService.java`
- DoctorService: `.../service/DoctorService.java`
- Dockerfile: `app/Dockerfile`
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
