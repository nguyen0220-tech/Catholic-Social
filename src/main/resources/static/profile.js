const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const messageBox = document.getElementById("messageBox");
const avatarPreview = document.getElementById("avatarPreview");

// === Load thông tin người dùng ===
async function loadUserProfile() {
    if (!accessToken) {
        alert("Vui lòng đăng nhập");
        return window.location.href = "/auth.html";
    }
    try {
        const res = await fetch(`${URL_BASE}/user/profile`);
        const data = await res.json();

        if (data.success && data.data) {
            const user = data.data;

            // Hiển thị dữ liệu cơ bản
            document.getElementById("firstName").value = user.firstName || "";
            document.getElementById("lastName").value = user.lastName || "";
            document.getElementById("email").value = user.email || "";
            document.getElementById("phone").value = user.phone || "";
            document.getElementById("address").value = user.address || "";
            document.getElementById("birthDate").value = user.birthDate || "";
            document.getElementById("gender").value = user.gender || "OTHER";

            // Hiển thị avatar
            if (user.avatarUrl) {
                avatarPreview.src = user.avatarUrl;
            } else {
                avatarPreview.src = "/icon/default-avatar.png";
            }
        } else {
            showMessage("Không thể tải thông tin cá nhân.", "error");
        }
    } catch (err) {
        showMessage("Lỗi kết nối tới server.", "error");
    }
}

// === Upload avatar ===
document.getElementById("uploadAvatarBtn").addEventListener("click", async () => {
    const fileInput = document.getElementById("avatarFile");
    if (!fileInput.files.length) {
        showMessage("Vui lòng chọn ảnh trước khi tải lên!", "error");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    try {
        const res = await fetch(`${URL_BASE}/user/upload-avatar`, {
            method: "POST",
            body: formData
        });

        const result = await res.json();

        if (res.ok && result.success) {
            showMessage(result.message, "success");

            // Cập nhật ảnh đại diện mới
            if (result.data) {
                avatarPreview.src = result.data;
            }
        } else {
            showMessage(result.message || "Không thể upload avatar", "error");
        }
    } catch (err) {
        showMessage("Lỗi khi upload avatar.", "error");
    }
});

// === Cập nhật thông tin cá nhân ===
document.getElementById("profileForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const profileData = {
        firstName: document.getElementById("firstName").value,
        lastName: document.getElementById("lastName").value,
        email: document.getElementById("email").value,
        phone: document.getElementById("phone").value,
        address: document.getElementById("address").value,
        birthDate: document.getElementById("birthDate").value,
        gender: document.getElementById("gender").value
    };

    try {
        const res = await fetch(`${URL_BASE}/user/update-profile`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(profileData)
        });

        const result = await res.json();

        if (res.ok && result.success) {
            showMessage(result.message || "Cập nhật thành công!", "success");
        } else if (result.data) {
            let errors = "";
            for (const [field, msg] of Object.entries(result.data)) {
                errors += `${field}: ${msg}<br>`;
            }
            showMessage(errors, "error");
        } else {
            showMessage(result.message || "Lỗi không xác định", "error");
        }
    } catch (err) {
        showMessage("Không thể kết nối tới server.", "error");
    }
});

const toggleBtn = document.getElementById("toggleExtraBtn");
const extraInfo = document.getElementById("extraInfo");

toggleBtn.addEventListener("click", () => {
    if (extraInfo.style.display === "none") {
        extraInfo.style.display = "block";
        toggleBtn.textContent = "Ẩn bớt thông tin";
    } else {
        extraInfo.style.display = "none";
        toggleBtn.textContent = "Xem thêm thông tin";
    }
});

// === Hiển thị thông báo ===
function showMessage(msg, type) {
    messageBox.innerHTML = msg;
    messageBox.className = type === "success" ? "success" : "error";
}

// === Gọi khi mở trang ===
loadUserProfile();
