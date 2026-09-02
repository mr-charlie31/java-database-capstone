async function getAllDoctors(token) {
  const res = await fetch(`${API_BASE_URL}/doctor`, {
    headers: { "Authorization": `Bearer ${token}` },
  });
  return res.ok ? res.json() : [];
}

async function searchDoctors(name = "", specialty = "", token) {
  const params = new URLSearchParams();
  if (name) params.append("name", name);
  if (specialty) params.append("specialty", specialty);
  const res = await fetch(`${API_BASE_URL}/doctor/search?${params}`, {
    headers: { "Authorization": `Bearer ${token}` },
  });
  return res.ok ? res.json() : [];
}

async function addDoctor(doctor, token) {
  const res = await fetch(`${API_BASE_URL}/doctor`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify(doctor),
  });
  return res;
}

async function updateDoctor(id, doctor, token) {
  const res = await fetch(`${API_BASE_URL}/doctor/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify(doctor),
  });
  return res;
}

async function deleteDoctor(id, token) {
  return fetch(`${API_BASE_URL}/doctor/${id}`, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${token}` },
  });
}