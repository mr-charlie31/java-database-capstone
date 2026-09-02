function openModal(contentHtml) {
  const root = document.getElementById("modalRoot");
  root.innerHTML = `
    <div class="modal-overlay">
      <div class="modal-box">
        <button class="modal-close" id="modalCloseBtn">&times;</button>
        ${contentHtml}
      </div>
    </div>`;
  document.getElementById("modalCloseBtn").addEventListener("click", closeModal);
}
function closeModal() {
  document.getElementById("modalRoot").innerHTML = "";
}
