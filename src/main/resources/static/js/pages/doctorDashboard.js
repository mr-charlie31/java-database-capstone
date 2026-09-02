document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const doctorId = localStorage.getItem("doctorId");
  const body = document.getElementById("appointmentBody");

  const appointments = await getAppointmentsByDoctor(doctorId, token);

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