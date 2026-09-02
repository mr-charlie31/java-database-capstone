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
