const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

let page = 0;
const size = 10;
let isLoading = false;
let isEnd = false;
let currentTab = "followers"; //  followers | blocked

const container = document.getElementById("followers-container");
const loading = document.getElementById("loading");
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");
const tabFollowers = document.getElementById("tabFollowers");
const tabBlocked = document.getElementById("tabBlocked");

// --- API: Lấy danh sách người theo dõi ---
async function fetchFollowers() {
    if (isLoading || isEnd) return;
    isLoading = true;
    loading.style.display = "block";

    try {
        const url =
            currentTab === "followers"
                ? `${URL_BASE}/follow?page=${page}&size=${size}`
                : `${URL_BASE}/follow/find-blocked?page=${page}&size=${size}`;

        const res = await fetch(url, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
        });

        const json = await res.json();
        if (!res.ok) throw new Error(json.message || "Lỗi tải dữ liệu");

        const list = json.data || [];
        if (list.length === 0) {
            isEnd = true;
            loading.innerText = "Đã tải hết.";
            return;
        }

        if (currentTab === "followers") {
            renderFollowers(list);
        } else {
            renderBlocked(list);
        }

        page++;
    } catch (err) {
        console.error("Fetch error:", err);
    } finally {
        isLoading = false;
        loading.style.display = "none";
    }
}

// --- API: Tìm người dùng ---
async function searchUsers(keyword) {
    if (!keyword.trim()) {
        resetAndLoad();
        return;
    }

    container.innerHTML = "";
    loading.style.display = "block";

    try {
        const res = await fetch(
            `${URL_BASE}/user/find-follow?keyword=${encodeURIComponent(
                keyword
            )}&page=0&size=${size}`,
            {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    "Content-Type": "application/json",
                },
            }
        );

        const json = await res.json();
        if (!res.ok) throw new Error(json.message || "Lỗi tìm người dùng");

        const users = json.data || [];
        if (users.length === 0) {
            container.innerHTML = `<p style="text-align:center;">Không tìm thấy người dùng nào.</p>`;
            return;
        }

        renderUserResults(users);
    } catch (err) {
        console.error("Search error:", err);
    } finally {
        loading.style.display = "none";
    }
}

// --- Reset danh sách và tải lại ---
function resetAndLoad() {
    container.innerHTML = "";
    page = 0;
    isEnd = false;
    loading.innerText = "Đang tải...";
    fetchFollowers();
}

//  HIỂN THỊ DANH SÁCH FOLLOWERS
function renderFollowers(followers) {
    followers.forEach((f) => {
        const avatarUrl =
            f.userAvatarUrl && f.userAvatarUrl.startsWith("http")
                ? f.userAvatarUrl
                : f.userAvatarUrl
                    ? `${URL_BASE}${f.userAvatarUrl}`
                    : "icon/default-avatar.png";

        const item = document.createElement("div");
        item.classList.add("follower-item");
        item.style.cssText = `
            display:flex; align-items:center; justify-content:space-between; gap:10px;
            border:1px solid #ddd; border-radius:8px;
            padding:10px; margin-bottom:8px; background-color:#fafafa;`;

        item.innerHTML = `
            <div style="display:flex; align-items:center; gap:10px;">
                <img src="${avatarUrl}" alt="${f.userName}" 
                     style="width:50px;height:50px;border-radius:50%;object-fit:cover;border:1px solid #ccc;">
                <a href="user.html?id=${f.userId}" style="text-decoration:none; color:#333; font-weight:bold;">
                    ${f.userName}
                </a>
            </div>
            <div style="display:flex; gap:6px;">
                <button class="unfollow-btn"
                        style="background:#dc3545;color:white;border:none;padding:6px 10px;border-radius:5px;cursor:pointer;">
                    Bỏ theo dõi
                </button>
                <button class="block-btn" 
                        style="background:#6c757d;color:white;border:none;padding:6px 10px;border-radius:5px;cursor:pointer;">
                    Chặn
                </button>
            </div>
        `;

        // Nút bỏ theo dõi
        item.querySelector(".unfollow-btn").addEventListener("click", () =>
            userAction(f.userId, "UNFOLLOW", item)
        );

        //  Nút chặn
        item.querySelector(".block-btn").addEventListener("click", () =>
            blockUser(f.userId, item)
        );

        container.appendChild(item);
    });
}

// --- HIỂN THỊ DANH SÁCH BỊ CHẶN ---
function renderBlocked(blockedUsers) {
    blockedUsers.forEach((u) => {
        const avatarUrl =
            u.userAvatarUrl && u.userAvatarUrl.startsWith("http")
                ? u.userAvatarUrl
                : u.userAvatarUrl
                    ? `${URL_BASE}${u.userAvatarUrl}`
                    : "icon/default-avatar.png";

        const item = document.createElement("div");
        item.classList.add("blocked-item");
        item.style.cssText = `
            display:flex; align-items:center; justify-content:space-between; gap:10px;
            border:1px solid #ddd; border-radius:8px;
            padding:10px; margin-bottom:8px; background-color:#fbeaea;`;

        item.innerHTML = `
            <div style="display:flex; align-items:center; gap:10px;">
                <img src="${avatarUrl}" alt="${u.userName}" 
                     style="width:50px;height:50px;border-radius:50%;object-fit:cover;border:1px solid #ccc;">
                <strong>${u.userName}</strong>
            </div>
            <button class="unblock-btn"
                    style="background:#28a745;color:white;border:none;padding:6px 10px;border-radius:5px;cursor:pointer;">
                Bỏ chặn
            </button>
        `;

        item.querySelector(".unblock-btn").addEventListener("click", () =>
            userAction(u.userId, "UNBLOCK", item)
        );

        container.appendChild(item);
    });
}

// --- GỌI API BỎ THEO DÕI / BỎ CHẶN ---
async function userAction(userId, action, itemElement) {
    if (!confirm(`Bạn có chắc muốn thực hiện hành động ${action}?`)) return;

    try {
        const res = await fetch(`${URL_BASE}/follow?userId=${userId}&action=${action}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            }
        });

        const json = await res.json();
        if (!res.ok || !json.success) {
            alert(json.message || "Thao tác thất bại!");
            return;
        }

        alert(json.message || "Thành công!");
        itemElement.remove();
    } catch (err) {
        console.error("Action error:", err);
        alert("Không thể kết nối máy chủ!");
    }
}

// GỌI API CHẶN NGƯỜI DÙNG
async function blockUser(userId, itemElement) {
    if (!confirm("Bạn có chắc muốn chặn người dùng này?")) return;

    try {
        const res = await fetch(`${URL_BASE}/follow/block?userId=${userId}`, {
            method: "PUT",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
        });

        const json = await res.json();
        if (!res.ok || !json.success) {
            alert(json.message || "Lỗi khi chặn người dùng!");
            return;
        }

        alert(json.message || "Đã chặn người dùng!");
        itemElement.remove();
    } catch (err) {
        console.error("Block error:", err);
        alert("Không thể kết nối đến máy chủ!");
    }
}

// HIỂN THỊ KẾT QUẢ TÌM KIẾM (Thêm nút Block)
function renderUserResults(users) {
    users.forEach((u) => {
        const fullName = `${u.firstName || ""} ${u.lastName || ""}`.trim() || "Không tên";
        const avatarUrl =
            u.userAvatarUrl && u.userAvatarUrl.startsWith("http")
                ? u.userAvatarUrl
                : u.userAvatarUrl
                    ? `${URL_BASE}${u.userAvatarUrl}`
                    : "icon/default-avatar.png";

        const item = document.createElement("div");
        item.classList.add("user-item");
        item.style.cssText = `
            display:flex; align-items:center; justify-content:space-between; gap:10px;
            border:1px solid #ddd; border-radius:8px; padding:10px; margin-bottom:8px; background-color:#f9f9f9;`;

        item.innerHTML = `
            <div style="display:flex; align-items:center; gap:10px;">
                <img src="${avatarUrl}" alt="${fullName}"
                     style="width:50px; height:50px; border-radius:50%; object-fit:cover; border:1px solid #ccc;">
                <div><strong>${fullName}</strong></div>
            </div>
            <div style="display:flex; gap:6px;">
                <button class="follow-btn"
                        style="background:#007bff; color:white; border:none; padding:6px 10px; border-radius:5px; cursor:pointer;">
                    Theo dõi
                </button>
                <button class="block-btn"
                        style="background:#6c757d; color:white; border:none; padding:6px 10px; border-radius:5px; cursor:pointer;">
                    Chặn
                </button>
            </div>
        `;

        // Nút theo dõi
        item.querySelector(".follow-btn").addEventListener("click", async () => {
            await followUser(u.id, item.querySelector(".follow-btn"));
        });

        // ✅ Nút chặn
        item.querySelector(".block-btn").addEventListener("click", () =>
            blockUser(u.id, item)
        );

        container.appendChild(item);
    });
}

// --- API gọi khi nhấn Theo dõi ---
async function followUser(userId, button) {
    button.disabled = true;
    button.innerText = "Đang theo dõi...";

    try {
        const res = await fetch(`${URL_BASE}/follow?userId=${userId}`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
        });

        const json = await res.json();
        if (!res.ok || !json.success) {
            alert(json.message || "Lỗi khi theo dõi người dùng!");
            button.disabled = false;
            button.innerText = "Theo dõi";
            return;
        }

        alert(json.message || "Theo dõi thành công!");
        button.innerText = "Đã theo dõi";
        button.style.background = "#6c757d";
        button.style.cursor = "default";
    } catch (err) {
        console.error("Follow error:", err);
        alert("Không thể kết nối đến máy chủ!");
        button.disabled = false;
        button.innerText = "Theo dõi";
    }
}

// --- Infinite scroll ---
window.addEventListener("scroll", () => {
    const scrollY = window.scrollY;
    const visible = window.innerHeight;
    const pageHeight = document.documentElement.scrollHeight;
    if (scrollY + visible >= pageHeight - 200 && !searchInput.value.trim()) {
        fetchFollowers();
    }
});

// --- Sự kiện tìm kiếm ---
searchBtn.addEventListener("click", () => searchUsers(searchInput.value));
searchInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") searchUsers(searchInput.value);
});

// --- Sự kiện chuyển tab ---
tabFollowers.addEventListener("click", () => {
    currentTab = "followers";
    tabFollowers.style.background = "#007bff";
    tabBlocked.style.background = "#6c757d";
    resetAndLoad();
});

tabBlocked.addEventListener("click", () => {
    currentTab = "blocked";
    tabBlocked.style.background = "#dc3545";
    tabFollowers.style.background = "#6c757d";
    resetAndLoad();
});

// --- Gọi lần đầu ---
fetchFollowers();
