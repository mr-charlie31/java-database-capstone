USE cms;

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
