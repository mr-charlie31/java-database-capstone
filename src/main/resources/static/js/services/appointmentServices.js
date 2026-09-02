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

async function getAppointmentsByDoctor(doctorId, token) {
  const res = await fetch(`${API_BASE_URL}/appointments/doctor/${doctorId}`, {
    headers: { "Authorization": `Bearer ${token}` },
  });
  return res.ok ? res.json() : [];
}
