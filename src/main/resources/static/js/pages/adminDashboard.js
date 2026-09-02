document.addEventListener("DOMContentLoaded", async () => {
  const token = localStorage.getItem("token");
  const list = document.getElementById("doctorList");
  const searchInput = document.getElementById("searchBar");
  const timeFilter = document.getElementById("timeFilter");
  const specialtyFilter = document.getElementById("specialtyFilter");

  async function refresh() {
    list.innerHTML = "";
    const doctors = await searchDoctors(searchInput.value, specialtyFilter.value, token);
    const filtered = timeFilter.value
      ? doctors.filter(d => d.availableTimes.some(t => t.startsWith(timeFilter.value)))
      : doctors;
    filtered.forEach(d => renderDoctorCard(d, list));
  }

  searchInput.addEventListener("input", refresh);
  timeFilter.addEventListener("change", refresh);
  specialtyFilter.addEventListener("change", refresh);

  document.getElementById("addDoctorBtn").addEventListener("click", () => {
    openModal(`
      <h2>Add Doctor</h2>
      <form id="addDoctorForm" novalidate>
        <input type="text" id="docName" placeholder="Name" required minlength="3">
        <input type="text" id="docSpecialty" placeholder="Specialty" required>
        <input type="email" id="docEmail" placeholder="Email" required>
        <input type="password" id="docPassword" placeholder="Password" required minlength="6">
        <input type="tel" id="docPhone" placeholder="Phone (10 digits)" pattern="\\d{10}" required>
        <div class="error-text" id="addDoctorError"></div>
        <button type="submit">Save</button>
      </form>
    `);

    document.getElementById("addDoctorForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const phone = document.getElementById("docPhone").value;
      const errorBox = document.getElementById("addDoctorError");
      if (!/^\d{10}$/.test(phone)) {
        errorBox.textContent = "Phone must be exactly 10 digits.";
        return;
      }
      const doctor = {
        name: document.getElementById("docName").value,
        specialty: document.getElementById("docSpecialty").value,
        email: document.getElementById("docEmail").value,
        password: document.getElementById("docPassword").value,
        phone,
        availableTimes: [],
      };
      const res = await addDoctor(doctor, token);
      if (res.ok) { closeModal(); refresh(); }
      else errorBox.textContent = "Could not add doctor. Check the fields.";
    });
  });

  refresh();
});