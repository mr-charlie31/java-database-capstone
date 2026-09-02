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
