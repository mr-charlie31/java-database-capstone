document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const patientId = localStorage.getItem("patientId");
  const list = document.getElementById("doctorList");
  const searchInput = document.getElementById("searchBar");
  const timeFilter = document.getElementById("timeFilter");
  const specialtyFilter = document.getElementById("specialtyFilter");

  async function refresh() {
    list.innerHTML = "";
    const doctors = await searchDoctors(searchInput.value, specialtyFilter.value, token);
    const filtered = timeFilter.value
      ? doctors.filter(d => (d.availableTimes || []).some(t => t.startsWith(timeFilter.value)))
      : doctors;
    filtered.forEach(d => renderDoctorCard(d, list));
  }

  searchInput.addEventListener("input", refresh);
  timeFilter.addEventListener("change", refresh);
  specialtyFilter.addEventListener("change", refresh);

  list.addEventListener("click", (e) => {
    if (!e.target.classList.contains("book-btn")) return;
    const doctorId = e.target.dataset.id;
    openBookingForm(doctorId);
  });

  function openBookingForm(doctorId) {
    const today = new Date().toISOString().split("T")[0];
    openModal(`
      <h2>Book <span class="highlight">Appointment</span></h2>
      <form id="bookForm" novalidate>
        <label>Date</label>
        <input type="date" id="apptDate" min="${today}" required>
        <label>Time</label>
        <select id="apptTime" required>
          <option value="">Select a time</option>
          <option value="09:00">09:00-10:00</option>
          <option value="10:00">10:00-11:00</option>
          <option value="11:00">11:00-12:00</option>
          <option value="14:00">14:00-15:00</option>
        </select>
        <div class="error-text" id="bookError"></div>
        <button type="submit" class="role-btn">Book Appointment</button>
      </form>
    `);

    document.getElementById("bookForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const date = document.getElementById("apptDate").value;
      const time = document.getElementById("apptTime").value;
      const errorBox = document.getElementById("bookError");

      if (!date || !time) {
        errorBox.textContent = "Please select both a date and a time.";
        return;
      }
      const appointmentTime = `${date}T${time}:00`;
      if (new Date(appointmentTime) <= new Date()) {
        errorBox.textContent = "Appointment time must be in the future.";
        return;
      }

      const dto = { doctorId, patientId, appointmentTime, status: 0 };
      const res = await bookAppointment(dto, token);
      if (res.ok) {
        closeModal();
        alert("Appointment booked successfully.");
      } else {
        const msg = await res.text();
        errorBox.textContent = msg || "Could not book this slot. Try another time.";
      }
    });
  }

  refresh();
});
