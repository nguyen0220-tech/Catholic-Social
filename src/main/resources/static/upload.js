const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const form = document.getElementById("momentForm");
const preview = document.getElementById("preview");
const messageBox = document.getElementById("messageBox");
const momentsList = document.getElementById("momentsList");

// --- Preview ảnh trước khi upload ---
document.getElementById("files").addEventListener("change", (event) => {
    preview.innerHTML = "";
    Array.from(event.target.files).forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.createElement("img");
            img.src = e.target.result;
            preview.appendChild(img);
        };
        reader.readAsDataURL(file);
    });
});

// --- Upload Moment ---
form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append("content", document.getElementById("content").value);
    formData.append("share", document.getElementById("share").value);

    const files = document.getElementById("files").files;
    for (let file of files) {
        formData.append("files", file);
    }

    try {
        const response = await fetch(`${URL_BASE}/moment`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${accessToken}` },
            body: formData
        });

        const result = await response.json();
        if (result.success) {
            messageBox.style.color = "green";
            messageBox.textContent = "Đăng bài thành công!";
            form.reset();
            preview.innerHTML = "";
            resetMoments(); // load lại từ đầu
        } else {
            messageBox.style.color = "red";
            messageBox.textContent = result.message || "Lỗi khi đăng bài!";
        }
    } catch (err) {
        messageBox.style.color = "red";
        messageBox.textContent = "Lỗi kết nối server!";
        console.error(err);
    }
});

// --- GLOBAL STATE ---
let currentPage = 0;
const pageSize = 5;
let loading = false;
let endReached = false;

// --- Load danh sách moment (phân trang) ---
async function loadMoments() {
    if (loading || endReached) return;
    loading = true;

    try {
        const res = await fetch(`${URL_BASE}/moment?page=${currentPage}&size=${pageSize}`, {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        if (!res.ok || !result.success) throw new Error(result.message || "Load moments failed");

        const data = result.data;
        if (data.length === 0) {
            endReached = true;
            if (currentPage === 0) {
                momentsList.innerHTML = "<p>Chưa có bài viết nào.</p>";
            }
            return;
        }

        renderMoments(data, currentPage > 0);
        currentPage++;
    } catch (error) {
        console.error(error);
        if (currentPage === 0)
            momentsList.innerHTML = "<p style='color:red'>Không tải được danh sách bài viết.</p>";
    } finally {
        loading = false;
    }
}

// --- Render danh sách moment ---
function renderMoments(moments, append = false) {
    if (!append) momentsList.innerHTML = "";

    moments.forEach(moment => {
        const div = document.createElement("div");
        div.className = "moment";
        div.innerHTML = `
      <div class="moment-header">
        <img class="moment-avatar" src="${moment.userAvatar || '/icon/default-avatar.png'}" alt="avatar">
        <div class="moment-header-info">
          <span class="moment-user">${moment.userFullName}</span>
          <div class="moment-meta">
            <span class="moment-time">${new Date(moment.createdAt).toLocaleString()}</span>
            ${moment.share ? `<span class="moment-share">· ${moment.share}</span>` : ""}
          </div>
        </div>
      </div>

      <div class="moment-content">${moment.content || ""}</div>

      <div class="moment-images">
        ${moment.imageUrls.map(url => `<img src="${url}" alt="moment image">`).join("")}
      </div>

      <div class="moment-actions">
        <button class="moment-btn edit-btn" data-id="${moment.id}" data-content="${moment.content}" data-share="${moment.share}">
          ✏️
        </button>
        <button class="moment-btn delete-btn" data-id="${moment.id}">
          🗑
        </button>
      </div>
    `;
        momentsList.appendChild(div);
    });

    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", () =>
            showEditForm(btn.dataset.id, btn.dataset.content, btn.dataset.share)
        );
    });

    document.querySelectorAll(".delete-btn").forEach(btn => {
        btn.addEventListener("click", () => deleteMoment(btn.dataset.id));
    });
}

// --- Reset danh sách ---
function resetMoments() {
    currentPage = 0;
    endReached = false;
    loadMoments();
}

// --- Infinite scroll ---
window.addEventListener("scroll", () => {
    const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 300;
    if (nearBottom) loadMoments();
});

// --- Modal chỉnh sửa ---
function showEditForm(momentId, oldContent, oldShare) {
    let modal = document.getElementById("editModal");
    if (!modal) {
        modal = document.createElement("div");
        modal.id = "editModal";
        modal.className = "modal";
        modal.innerHTML = `
      <div class="modal-content">
        <h3>Chỉnh sửa bài viết</h3>
        <textarea id="editContent" rows="4"></textarea>
        <select id="editShare">
          <option value="PUBLIC">Công khai</option>
          <option value="FOLLOWER">Người theo dõi</option>
          <option value="PRIVATE">Chỉ mình tôi</option>
        </select>
        <div class="modal-actions">
          <button id="saveEdit">Lưu</button>
          <button id="cancelEdit">Hủy</button>
        </div>
      </div>
    `;
        document.body.appendChild(modal);
    }

    document.getElementById("editContent").value = oldContent || "";
    document.getElementById("editShare").value = oldShare || "PUBLIC";
    modal.style.display = "flex";

    document.getElementById("saveEdit").onclick = () => {
        const newContent = document.getElementById("editContent").value.trim();
        const newShare = document.getElementById("editShare").value;
        if (!newContent) {
            alert("Nội dung không được để trống!");
            return;
        }

        updateMoment({ momentId: Number(momentId), content: newContent, share: newShare });
        modal.style.display = "none";
    };

    document.getElementById("cancelEdit").onclick = () => {
        modal.style.display = "none";
    };
}

// --- Cập nhật moment ---
async function updateMoment(requestBody) {
    try {
        const res = await fetch(`${URL_BASE}/moment`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert(result.message);
            resetMoments();
        } else {
            alert(result.message || "Cập nhật thất bại!");
        }
    } catch (err) {
        console.error(err);
        alert("Lỗi kết nối server!");
    }
}

// --- Xóa moment ---
async function deleteMoment(momentId) {
    if (!confirm("Bạn có chắc muốn xóa bài viết này không?")) return;

    try {
        const res = await fetch(`${URL_BASE}/moment/${momentId}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${accessToken}` }
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert(result.message);
            resetMoments();
        } else {
            alert(result.message || "Xóa thất bại!");
        }
    } catch (err) {
        console.error(err);
        alert("Lỗi kết nối server!");
    }
}

loadMoments();
