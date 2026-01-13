const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

const messageBox = document.getElementById("messageBox");
const avatarPreview = document.getElementById("avatarPreview");
const avatarFile = document.getElementById("avatarFile");

const toggleBtn = document.getElementById("toggleExtraBtn");
const extraInfo = document.getElementById("extraInfo");

/* ======================
   LOAD USER PROFILE
====================== */
async function loadUserProfile() {
    if (!accessToken) {
        alert("Vui lòng đăng nhập");
        location.href = "/auth.html";
        return;
    }

    try {
        const res = await fetch(`${URL_BASE}/user/profile`);
        const result = await res.json();

        if (!res.ok || !result.success) {
            showMessage(result.message || "Không thể tải thông tin cá nhân", "error");
            return;
        }

        const user = result.data;
        firstName.value = user.firstName || "";
        lastName.value = user.lastName || "";
        bio.value = user.bio || "";
        email.value = user.email || "";
        phone.value = user.phone || "";
        address.value = user.address || "";
        birthDate.value = user.birthDate || "";
        gender.value = user.gender || "UNKNOWN";
        avatarPreview.src = user.avatarUrl || "/icon/default-avatar.png";

    } catch (e) {
        showMessage("Lỗi kết nối tới server.", "error");
    }
}

/* ======================
   UPLOAD AVATAR
====================== */
document.getElementById("uploadAvatarBtn")?.addEventListener("click", async () => {
    if (!avatarFile.files.length) {
        showMessage("Vui lòng chọn ảnh trước khi tải lên!", "error");
        return;
    }

    const formData = new FormData();
    formData.append("file", avatarFile.files[0]);

    try {
        const res = await fetch(`${URL_BASE}/user/upload-avatar`, {
            method: "POST",
            body: formData
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
            showMessage(result.message || "Upload avatar thất bại", "error");
            return;
        }

        avatarPreview.src = result.data;
        showMessage(result.message || "Cập nhật avatar thành công", "success");

    } catch {
        showMessage("Lỗi khi upload avatar.", "error");
    }
});

/* ======================
   UPDATE PROFILE
====================== */
document.getElementById("profileForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const profileData = {
        firstName: firstName.value,
        lastName: lastName.value,
        bio: bio.value,
        email: email.value,
        phone: phone.value,
        address: address.value,
        birthDate: birthDate.value,
        gender: gender.value
    };

    try {
        const res = await fetch(`${URL_BASE}/user/update-profile`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(profileData)
        });

        const result = await res.json();

        // ❌ Lỗi validate
        if (!res.ok || !result.success) {
            if (result.data && typeof result.data === "object") {
                const errors = Object.entries(result.data)
                    .map(([f, m]) => `• ${m}`)
                    .join("<br>");
                showMessage(errors, "error");
            } else {
                showMessage(result.message || "Cập nhật thất bại", "error");
            }
            return;
        }

        // ✅ Thành công
        showMessage(result.message || "Cập nhật thành công!", "success");

    } catch {
        showMessage("Không thể kết nối tới server.", "error");
    }
});

toggleBtn.addEventListener("click", () => {
    const show = extraInfo.style.display === "none";
    extraInfo.style.display = show ? "block" : "none";
    toggleBtn.textContent = show ? "Ẩn bớt thông tin" : "Xem thêm thông tin";
});

function showMessage(msg, type) {
    messageBox.innerHTML = msg;
    messageBox.className = type; // success | error
}

loadUserProfile();
