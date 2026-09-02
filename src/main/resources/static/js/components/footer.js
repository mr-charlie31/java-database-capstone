function renderFooter() {
  document.getElementById("footer").innerHTML = `
    <div><strong>Company</strong><br><a href="#">About</a></div>
    <div><strong>Support</strong><br><a href="#">Account</a></div>
    <div><strong>Legals</strong><br><a href="#">Terms &amp; Conditions</a></div>
  `;
}
document.addEventListener("DOMContentLoaded", renderFooter);