const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

const messageBox = document.getElementById("messageBox");
const avatarPreview = document.getElementById("avatarPreview");
const avatarFile = document.getElementById("avatarFile");

const toggleBtn = document.getElementById("toggleExtraBtn");
const extraInfo = document.getElementById("extraInfo");

const introFile = document.getElementById("introFile");
const introPreview = document.getElementById("introPreview");
const introExpDay = document.getElementById("introExpDay");
const uploadIntroBtn = document.getElementById("uploadIntroBtn");
const removeIntroBtn = document.getElementById("removeIntroBtn");

let currentIntroId = null;

introFile.addEventListener("change", () => {
    const file = introFile.files[0];
    if (!file) return;

    introPreview.src = URL.createObjectURL(file);
    introPreview.style.display = "block";
});

uploadIntroBtn.addEventListener("click", async () => {
    const file = introFile.files[0];
    if (!file) {
        showMessage("Vui lòng chọn video", "error");
        return;
    }

    const formData = new FormData();
    formData.append("intro", file);
    formData.append("expDay", introExpDay.value);

    try {
        const res = await fetch(`${URL_BASE}/intro`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`
            },
            body: formData
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
            showMessage(result.message || "Upload video thất bại", "error");
            return;
        }

        const {id, url, exp} = result.data;

        currentIntroId = id;

        introPreview.src = url;
        introPreview.style.display = "block";
        introPreview.load();

        if (exp) {
            introExpText.innerText = formatIntroExp(exp);
            introExpText.style.display = "block";
        }

        removeIntroBtn.style.display = "block";


        showMessage("Upload video giới thiệu thành công", "success");

    } catch (e) {
        showMessage("Lỗi khi upload video", "error");
    }
});

removeIntroBtn.addEventListener("click", async () => {
    if (!currentIntroId) return;

    if (!confirm("Bạn chắc chắn muốn xóa video giới thiệu?")) return;

    try {
        const res = await fetch(
            `${URL_BASE}/intro/remove/${currentIntroId}`,
            {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            }
        );

        const result = await res.json();

        if (!res.ok || !result.success) {
            showMessage(result.message || "Xóa video thất bại", "error");
            return;
        }

        // reset UI
        introPreview.src = "";
        introPreview.style.display = "none";
        introFile.value = "";
        currentIntroId = null;
        removeIntroBtn.style.display = "none";

        showMessage("Đã xóa video giới thiệu", "success");

    } catch {
        showMessage("Lỗi khi xóa video", "error");
    }
});

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

        const introExpText = document.getElementById("introExpText");

        if (user.intro && user.intro.url) {
            currentIntroId = user.intro.id;

            introPreview.src = user.intro.url;
            introPreview.style.display = "block";
            introPreview.load();

            introPlaceholder.style.display = "none";
            removeIntroBtn.style.display = "block";

            if (user.intro.exp) {
                introExpText.innerText = formatIntroExp(user.intro.exp);
                introExpText.style.display = "block";
            } else {
                introExpText.style.display = "none";
            }

        } else {
            introPreview.style.display = "none";
            introPlaceholder.style.display = "block";
            introExpText.style.display = "none";
        }

    } catch (e) {
        showMessage("Lỗi kết nối tới server.", "error");
    }
}

const introClick = document.getElementById("introClick");
const introUploadPanel = document.getElementById("introUploadPanel");
const introPlaceholder = document.getElementById("introPlaceholder");

introClick.addEventListener("click", () => {
    const show = introUploadPanel.style.display === "none";
    introUploadPanel.style.display = show ? "block" : "none";
});

function formatIntroExp(exp) {
    const expDate = new Date(exp);
    const now = new Date();

    const diffMs = expDate - now;
    const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));

    const dateText = expDate.toLocaleDateString("vi-VN", {
        year: "numeric",
        month: "long",
        day: "numeric"
    });

    if (diffDays > 0) {
        return `⏳ Hết hạn sau ${diffDays} ngày (đến ${dateText})`;
    } else {
        return `⚠️ Video đã hết hạn (${dateText})`;
    }
}

async function fetchIntroCanRestore(page = 0, size = 10) {
    const query = `
        query($page: Int!, $size: Int!) {
            allVideosRestore(page: $page, size: $size) {
                data {
                    id
                    content {
                        url
                        exp
                        publicId
                    }
                }
                pageInfo {
                    page
                    size
                    hasNext
                }
            }
        }
    `;

    const res = await fetch(`${URL_BASE}/graphql`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`
        },
        body: JSON.stringify({
            query,
            variables: {page, size}
        })
    });

    const json = await res.json();

    if (json.errors) {
        console.error(json.errors);
        throw new Error("GraphQL error");
    }

    return json.data.allVideosRestore;
}

function renderRestoreIntroList(data) {
    const container = document.getElementById("restoreIntroList");

    if (!data.data.length) {
        container.innerHTML = `<p style="color:#888">Không có video nào</p>`;
        return;
    }

    container.innerHTML = data.data.map(item => {
        const { url, exp } = item.content;

        return `
            <div class="restore-intro-item"
                 id="restore-intro-${item.id}"
                 style="margin-bottom:16px">

                <video src="${url}" controls
                       style="width:100%; border-radius:12px"></video>

                <div style="font-size:0.85rem; color:#666; margin-top:4px">
                    ${formatIntroExp(exp)}
                </div>

                <button class="btn-primary" style="margin-top:6px"
                        onclick="restoreIntro(${item.id})">
                    ♻ Khôi phục video này
                </button>
            </div>
        `;
    }).join("");
}

const toggleRestoreBtn = document.getElementById("toggleRestoreIntroBtn");
const restorePanel = document.getElementById("restoreIntroPanel");

let restoreLoaded = false;

toggleRestoreBtn.addEventListener("click", async () => {
    const show = restorePanel.style.display === "none";
    restorePanel.style.display = show ? "block" : "none";

    if (show && !restoreLoaded) {
        try {
            toggleRestoreBtn.innerText = "⏳ Đang tải...";
            const data = await fetchIntroCanRestore();
            renderRestoreIntroList(data);
            restoreLoaded = true;
            toggleRestoreBtn.innerText = "📦 Video giới thiệu đã xóa";
        } catch {
            showMessage("Không thể tải danh sách video", "error");
            toggleRestoreBtn.innerText = "📦 Video giới thiệu đã xóa";
        }
    }
});

async function restoreIntro(introId) {
    if (!confirm("Bạn chắc chắn muốn khôi phục video này?")) return;

    try {
        const res = await fetch(`${URL_BASE}/intro/restore/${introId}`, {
            method: "PUT",
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
            showMessage(result.message || "Khôi phục thất bại", "error");
            return;
        }

        showMessage("Khôi phục video thành công", "success");

        document.getElementById(`restore-intro-${introId}`)?.remove();

        const container = document.getElementById("restoreIntroList");
        if (!container.children.length) {
            container.innerHTML = `<p style="color:#888">Không có video nào</p>`;
        }

    } catch (e) {
        showMessage("Lỗi khi khôi phục video", "error");
    }
}

window.restoreIntro = restoreIntro

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
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(profileData)
        });

        const result = await res.json();

        // ❌ Lỗi validate
        if (!res.ok || !result.success) {
            if (result.data && typeof result.data === "object") {
                const errors = Object.entries(result.data)
                    .map(([, m]) => `• ${m}`)
                    .join("<br>");
                showMessage(errors, "error");
            } else {
                showMessage(result.message || "Cập nhật thất bại", "error");
            }
            return;
        }

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

const avatarClick = document.getElementById("avatarClick");

avatarClick.addEventListener("click", () => {
    avatarFile.click();
});

avatarFile.addEventListener("change", () => {
    const file = avatarFile.files[0];
    if (file) {
        avatarPreview.src = URL.createObjectURL(file);
    }
});

avatarFile.addEventListener("change", async () => {
    const file = avatarFile.files[0];
    if (!file) return;

    avatarPreview.src = URL.createObjectURL(file);

    const formData = new FormData();
    formData.append("file", file);

    try {
        const res = await fetch(`${URL_BASE}/user/upload-avatar`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`
            },
            body: formData
        });

        const result = await res.json();
        if (!res.ok || !result.success) {
            showMessage(result.message || "Upload avatar thất bại", "error");
            return;
        }

        const newAvatarUrl = result.data;

        localStorage.setItem("userAvatar", newAvatarUrl);

        avatarPreview.src = newAvatarUrl;
        showMessage("Cập nhật avatar thành công", "success");
    } catch {
        showMessage("Lỗi khi upload avatar.", "error");
    }
});

loadUserProfile();
