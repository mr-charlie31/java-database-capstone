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
