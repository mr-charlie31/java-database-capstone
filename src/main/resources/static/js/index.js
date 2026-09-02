document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("adminBtn").addEventListener("click", () => showLoginForm("admin"));
  document.getElementById("doctorBtn").addEventListener("click", () => showLoginForm("doctor"));
  document.getElementById("patientBtn").addEventListener("click", () => showLoginForm("patient"));
});

function showLoginForm(role) {
  openModal(`
    <h2>${role.charAt(0).toUpperCase() + role.slice(1)} Login</h2>
    <form id="loginForm" novalidate>
      <label>Email</label>
      <input type="email" id="email" required>
      <div class="error-text" id="emailError"></div>

      <label>Password</label>
      <input type="password" id="password" minlength="6" required>
      <div class="error-text" id="passwordError"></div>

      <button type="submit">Login</button>
    </form>
  `);

  document.getElementById("loginForm").addEventListener("submit", (e) => {
    e.preventDefault();
    if (validateLoginForm()) login(role);
  });
}

function validateLoginForm() {
  let valid = true;
  const email = document.getElementById("email");
  const password = document.getElementById("password");
  const emailError = document.getElementById("emailError");
  const passwordError = document.getElementById("passwordError");

  emailError.textContent = "";
  passwordError.textContent = "";
  email.classList.remove("invalid");
  password.classList.remove("invalid");

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailPattern.test(email.value)) {
    emailError.textContent = "Enter a valid email address.";
    email.classList.add("invalid");
    valid = false;
  }
  if (password.value.length < 6) {
    passwordError.textContent = "Password must be at least 6 characters.";
    password.classList.add("invalid");
    valid = false;
  }
  return valid;
}

async function login(role) {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  try {
    const res = await fetch(`${API_BASE_URL}/${role}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!res.ok) throw new Error("Invalid credentials");
    const data = await res.json();

    localStorage.setItem("token", data.token);
    localStorage.setItem("role", role);
    window.location.href = `/${role}Dashboard/${data.token}`;
  } catch (err) {
    document.getElementById("passwordError").textContent = err.message;
  }
}