const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

const contentInput = document.getElementById("momentContent");
const filesInput = document.getElementById("momentFiles");
const shareSelect = document.getElementById("momentShare");
const postBtn = document.getElementById("postMomentBtn");
const currentUserId = Number(localStorage.getItem("userId"));

const GRAPHQL_URL = `${URL_BASE}/graphql`;
async function graphqlRequest(query, variables = {}) {
    const res = await fetch(GRAPHQL_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${accessToken}`
        },
        body: JSON.stringify({ query, variables })
    });
    return await res.json();
}

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
              style="width:100%; max-height:300px; object-fit:cover; border-radius:6px; margin-top:6px;" alt="">`
    ).join('');

    const editedText = moment.edited ? `<span style="color:#777; font-style:italic;">(Đã chỉnh sửa)</span>` : "";

    item.innerHTML = `
        <div style="display:flex; align-items:center; gap:10px; margin-bottom:6px;">
            <img src="${
        moment.userAvatar
            ? (moment.userAvatar.startsWith('http') ? moment.userAvatar : URL_BASE + moment.userAvatar)
            : 'icon/default-avatar.png'
    }"
            style="width:40px; height:40px; border-radius:50%; object-fit:cover; border:1px solid #ccc;" alt="">
            <strong>${moment.userFullName}</strong>
        </div>

        <div style="margin-bottom:6px;">${moment.content || ''}</div>
        ${imagesHtml}

        <div style="margin-top:6px; font-size:12px; color:#555;">
            ${formatDateTime(moment.createdAt)} - Chia sẻ: ${moment.share} ${editedText}
        </div>
        
        <div style="margin-top:8px; display:flex; gap:10px; align-items:center;">
            <button class="heart-btn" 
                    data-moment-id="${moment.id}" 
                    style="border:none;background:none;font-size:20px;cursor:pointer;">
                🤍
            </button>
            <span class="heart-count" id="heart-count-${moment.id}">0</span>
            
            <div class="heart-users" 
                 id="heart-users-${moment.id}" 
                 style="display:none;margin-top:6px;background:#fff;border:1px solid #ddd;
                        border-radius:6px;padding:6px;">
            </div>

        </div>

        
        <div id="comments-${moment.id}" class="moment-comments" 
             style="margin-top:10px;padding:8px;background:#f0f0f0;border-radius:8px;">
          <div class="comment-list"></div>
          <div class="comment-form" style="display:flex;gap:6px;margin-top:6px;">
            <input type="text" class="comment-input" placeholder="Viết bình luận..."
                   style="flex:1;padding:6px;border-radius:6px;border:1px solid #ccc;">
            <button class="comment-send" data-moment-id="${moment.id}"
                    style="background:#007bff;color:#fff;border:none;padding:6px 10px;border-radius:6px;cursor:pointer;">Gửi</button>
          </div>
        </div>

    `;

    momentsContainer.appendChild(item);
    renderComments(moment.id);
    renderHearts(moment.id)
}

// --- Lấy danh sách Moment từ API ---
async function fetchMoments() {
    if (!accessToken) {
        alert("Vui lòng đăng nhập");
        return window.location.href = "/auth.html";
    }

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

// ===== GRAPHQL COMMENT =====
const GET_COMMENTS = `
  query GetComments($momentId: ID!, $page: Int!, $size: Int!) {
    getComments(momentId: $momentId, page: $page, size: $size) {
      data {
        id
        comment
        commentDate
        user {
          id
          userFullName
          avatarUrl
        }
      }
    }
  }
`;

const CREATE_COMMENT = `
  mutation CreateComment($momentId: ID!, $comment: String!) {
    createComment(
      momentId: $momentId,
      request: { comment: $comment }
    ) {
      success
      message
    }
  }
`;

async function renderComments(momentId) {
    const container = document.querySelector(`#comments-${momentId} .comment-list`);
    if (!container) return;

    const res = await graphqlRequest(GET_COMMENTS, {
        momentId,
        page: 0,
        size: 5
    });

    const comments = res.data?.getComments?.data || [];
    container.innerHTML = "";

    comments.forEach(c => {
        const div = document.createElement("div");
        div.style.cssText = `display:flex; gap:6px; margin-bottom:6px; align-items:flex-start;`;

        div.innerHTML = `
            <img src="${
            c.user.avatarUrl
                ? (c.user.avatarUrl.startsWith('http') ? c.user.avatarUrl : URL_BASE + c.user.avatarUrl)
                : 'icon/default-avatar.png'
        }" style="width:32px;height:32px;border-radius:50%;object-fit:cover;cursor:pointer;" 
           onclick="goToProfile(${c.user.id})">

            <div style="background:#fff;padding:6px 8px;border-radius:6px;flex:1;">
                <strong style="font-size:13px;cursor:pointer;" onclick="goToProfile(${c.user.id})">
                    ${c.user.userFullName}
                </strong>
                <div style="font-size:14px;">${c.comment}</div>
                <div style="font-size:11px;color:#777;">
                    ${formatDateTime(c.commentDate)}
                </div>
            </div>
        `;

        container.appendChild(div);
    });
}

momentsContainer.addEventListener("click", async (e) => {
    const sendBtn = e.target.closest(".comment-send");
    if (!sendBtn) return;

    const momentId = sendBtn.dataset.momentId;
    const wrapper = document.getElementById(`comments-${momentId}`);
    const input = wrapper.querySelector(".comment-input");

    const text = input.value.trim();
    if (!text) return;

    const res = await graphqlRequest(CREATE_COMMENT, {
        momentId,
        comment: text
    });

    if (res.data?.createComment?.success) {
        input.value = "";
        renderComments(momentId); // reload comment
    } else {
        alert(res.data?.createComment?.message || "Gửi comment thất bại!");
    }
});

// ===== GRAPHQL HEART =====
const GET_HEARTS = `
  query GetHearts($momentId: ID!, $page: Int!, $size: Int!) {
    getHeartsByMomentId(momentId: $momentId, page: $page, size: $size) {
      data {
        heartId
        user {
          id
          userFullName
          avatarUrl
        }
      }
    }
  }
`;


const ADD_HEART = `
  mutation AddHeart($momentId: ID!) {
    addHeart(momentId: $momentId) {
      success
      message
    }
  }
`;

const DELETE_HEART = `
  mutation DeleteHeart($momentId: ID!) {
    deleteHeart(momentId: $momentId) {
      success
      message
    }
  }
`;

async function renderHearts(momentId) {
    const countEl = document.getElementById(`heart-count-${momentId}`);
    const btn = document.querySelector(`.heart-btn[data-moment-id="${momentId}"]`);

    if (!countEl || !btn) return;

    const res = await graphqlRequest(GET_HEARTS, {
        momentId,
        page: 0,
        size: 100
    });

    if (res.errors?.length) {
        console.error(res.errors);
        return;
    }

    const hearts = res.data?.getHeartsByMomentId?.data || [];
    countEl.innerText = hearts.length;

    const isLiked = hearts.some(h => Number(h.user.id) === currentUserId);
    btn.innerText = isLiked ? "❤️" : "🤍";
    btn.dataset.liked = isLiked ? "true" : "false";

    //  render danh sách user
    renderHeartUsers(momentId, hearts);
}


momentsContainer.addEventListener("click", async (e) => {
    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    const momentId = heartBtn.dataset.momentId;
    const liked = heartBtn.dataset.liked === "true";

    let res;
    if (liked) {
        res = await graphqlRequest(DELETE_HEART, { momentId });
    } else {
        res = await graphqlRequest(ADD_HEART, { momentId });
    }

    if (res.errors?.length) {
        console.error(res.errors);
        alert(res.errors[0].message);
        return;
    }

    const result = liked
        ? res.data?.deleteHeart
        : res.data?.addHeart;

    if (result?.success) {
        renderHearts(momentId);
    } else {
        alert(result?.message || "Lỗi xử lý heart");
    }
});

function renderHeartUsers(momentId, hearts) {
    const box = document.getElementById(`heart-users-${momentId}`);
    if (!box) return;

    if (hearts.length === 0) {
        box.innerHTML = "<i style='color:#777'>Chưa có ai thả tim</i>";
        return;
    }

    box.innerHTML = hearts.map(h => `
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;cursor:pointer;" 
             onclick="goToProfile(${h.user.id})">
            <img src="${
        h.user.avatarUrl
            ? (h.user.avatarUrl.startsWith("http") ? h.user.avatarUrl : URL_BASE + h.user.avatarUrl)
            : "/icon/default-avatar.png"
    }" style="width:28px;height:28px;border-radius:50%;object-fit:cover;">
            <span style="font-size:14px;">${h.user.userFullName}</span>
        </div>
    `).join("");
}

momentsContainer.addEventListener("click", (e) => {
    const countEl = e.target.closest(".heart-count");
    if (!countEl) return;

    const momentId = countEl.id.replace("heart-count-", "");
    const box = document.getElementById(`heart-users-${momentId}`);
    if (!box) return;

    box.style.display = box.style.display === "none" ? "block" : "none";
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
            body: JSON.stringify({ refreshToken })
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

function goToProfile(userId) {
    window.location.href = `/user.html?id=${userId}`;
}

window.goToProfile = goToProfile;

fetchMoments();