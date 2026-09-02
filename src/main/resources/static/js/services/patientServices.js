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