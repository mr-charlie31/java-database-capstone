function renderDoctorCard(doctor, container) {
  const card = document.createElement("div");
  card.className = "doctor-card";
  card.innerHTML = `
    <h3>${doctor.name}</h3>
    <p>Specialization: ${doctor.specialty}</p>
    <p>Email: ${doctor.email}</p>
    <p>Available: ${doctor.availableTimes.join(", ")}</p>
    <button class="book-btn" data-id="${doctor.id}">Book Now</button>
  `;
  container.appendChild(card);
}