## MySQL Database Design

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

### Table: doctor_available_times
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

Design notes:
- `appointments` uses `ON DELETE CASCADE` on both foreign keys so a deleted doctor/patient doesn't leave orphan rows.
- `email` is `UNIQUE` on both `doctors` and `patients` since it doubles as the login identifier.
- Overlapping appointments for one doctor are prevented in the service layer (query existing appointments for that doctor/time window before insert).

## MongoDB Collection Design

### Collection: prescriptions
```json
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
- Only `appointmentId` (not the full appointment object) is stored — MongoDB keeps a lightweight reference, MySQL remains the source of truth for relational data.
- `pharmacy` is embedded since it's always read together with the prescription.
- `tags` as an array supports future filtering without a schema migration.
