const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

const contentInput = document.getElementById("momentContent");
const filesInput = document.getElementById("momentFiles");
const shareSelect = document.getElementById("momentShare");
const postBtn = document.getElementById("postMomentBtn");

postBtn.addEventListener("click", async () => {
    const content = contentInput.value.trim();
    const share = shareSelect.value;
    const files = filesInput.files;

    if (!share) {
        alert("Vui lòng chọn chế độ chia sẻ!");
        return;
    }

    const formData = new FormData();
    formData.append("content", content);
    formData.append("share", share);
    for (let i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
    }

    postBtn.disabled = true;
    postBtn.innerText = "Đang đăng...";

    try {
        const res = await fetch(`${URL_BASE}/moment`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`
            },
            body: formData
        });

        const json = await res.json();

        if (!res.ok || !json.success) {
            alert(json.message || "Đăng khoảnh khắc thất bại!");
            return;
        }

        alert(json.message || "Đăng khoảnh khắc thành công!");
        contentInput.value = "";
        filesInput.value = "";
        shareSelect.value = "";
        location.reload(); // Tải lại trang để cập nhật danh sách moment

    } catch (err) {
        console.error("Upload moment error:", err);
        alert("Không thể kết nối đến máy chủ!");
    } finally {
        postBtn.disabled = false;
        postBtn.innerText = "Đăng khoảnh khắc";
    }
});

// --- Danh sách Moment ---
const momentsContainer = document.getElementById("momentsContainer");
const momentsLoading = document.getElementById("momentsLoading");

let momentPage = 0;
const momentSize = 5;
let momentIsLoading = false;
let momentIsEnd = false;

// --- Format thời gian hiển thị ---
function formatDateTime(datetimeStr) {
    const date = new Date(datetimeStr);
    return date.toLocaleString();
}

// --- Hiển thị một Moment ---
function renderMoment(moment) {
    const item = document.createElement("div");
    item.style.cssText = `
        border:1px solid #ddd; border-radius:8px; padding:10px; background:#f9f9f9;
    `;

    const imagesHtml = moment.imageUrls.map(url =>
        `<img src="${url.startsWith('http') ? url : URL_BASE + url}" 
              style="width:100%; max-height:300px; object-fit:cover; border-radius:6px; margin-top:6px;">`
    ).join('');

    item.innerHTML = `
        <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
            <img src="${moment.userAvatar ? (moment.userAvatar.startsWith('http') ? moment.userAvatar : URL_BASE + moment.userAvatar) : 'icon/default-avatar.png'}"
                 style="width:40px; height:40px; border-radius:50%; object-fit:cover; border:1px solid #ccc;">
            <strong>${moment.userFullName}</strong>
        </div>
        <div style="margin-bottom:6px;">${moment.content || ''}</div>
        ${imagesHtml}
        <div style="margin-top:6px; font-size:12px; color:#555;">${formatDateTime(moment.createdAt)} - Chia sẻ: ${moment.share}</div>
    `;

    momentsContainer.appendChild(item);
}

// --- Lấy danh sách Moment từ API ---
async function fetchMoments() {
    if (momentIsLoading || momentIsEnd) return;
    momentIsLoading = true;
    momentsLoading.style.display = "block";
    momentsLoading.innerText = "Đang tải...";

    try {
        const res = await fetch(`${URL_BASE}/moment/all?page=${momentPage}&size=${momentSize}`, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            }
        });
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.message || "Lỗi tải Moment");

        const list = json.data || [];
        if (list.length === 0) {
            momentIsEnd = true;
            momentsLoading.innerText = "Đã tải hết Moment.";
            return;
        }

        list.forEach(renderMoment);
        momentPage++;
    } catch (err) {
        console.error("Fetch moments error:", err);
        momentsLoading.innerText = "Lỗi tải Moment.";
    } finally {
        momentIsLoading = false;
    }
}

// --- Infinite scroll cho Moment ---
window.addEventListener("scroll", () => {
    if (window.scrollY + window.innerHeight >= document.documentElement.scrollHeight - 200) {
        fetchMoments();
    }
});

// --- Đăng xuất ---
const logoutBtn = document.getElementById("logoutBtn");

logoutBtn.addEventListener("click", async () => {
    if (!confirm("Bạn có chắc muốn đăng xuất không?")) return;

    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
        alert("Không tìm thấy refreshToken. Vui lòng đăng nhập lại!");
        return;
    }

    try {
        const res = await fetch(`${URL_BASE}/auth/logout`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ refreshToken }) // 👈 dùng refreshToken thay vì accessToken
        });

        const json = await res.json();

        if (!res.ok || !json.success) {
            alert(json.message || "Đăng xuất thất bại!");
            return;
        }

        // Xóa token khỏi localStorage và chuyển hướng
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("userId");

        alert(json.message || "Đăng xuất thành công!");
        window.location.href = "auth.html";

    } catch (err) {
        console.error("Logout error:", err);
        alert("Không thể kết nối đến máy chủ!");
    }
});

// --- Load lần đầu ---
fetchMoments();