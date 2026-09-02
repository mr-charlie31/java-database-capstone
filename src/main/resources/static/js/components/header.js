function renderHeader() {
  const header = document.getElementById("header");
  const role = localStorage.getItem("role");

  header.innerHTML = `
    <div class="brand">🏥 Hospital CMS</div>
    <nav>
      ${role
        ? `<a href="#" id="logoutLink">Logout</a>`
        : `<a href="/login">Login</a> <a href="/signup">Sign Up</a>`}
    </nav>
  `;

  const logoutLink = document.getElementById("logoutLink");
  if (logoutLink) {
    logoutLink.addEventListener("click", () => {
      localStorage.removeItem("token");
      localStorage.removeItem("role");
      window.location.href = "/login";
    });
  }
}
document.addEventListener("DOMContentLoaded", renderHeader);