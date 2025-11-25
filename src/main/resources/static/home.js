const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");

const contentInput = document.getElementById("momentContent");
const filesInput = document.getElementById("momentFiles");
const shareSelect = document.getElementById("momentShare");
const postBtn = document.getElementById("postMomentBtn");

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

async function loadHearts(momentId) {
    const query = `
      query GetHearts($momentId: ID, $page: Int!, $size: Int!) {
        getHeartsByMomentId(momentId: $momentId, page: $page, size: $size) {
          heartId
          user {
            id
            userFullName
            avatarUrl
          }
        }
      }
    `;

    try {
        const result = await graphqlRequest(query, { momentId, page: 0, size: 50 });
        return result.data?.getHeartsByMomentId || [];
    } catch (err) {
        console.error("Load hearts error:", err);
        return [];
    }
}


async function renderHearts(momentId) {
    const hearts = await loadHearts(momentId);

    // Count
    const countSpan = document.getElementById(`heart-count-${momentId}`);
    countSpan.innerText = hearts.length;

    // Toggle button
    const myUserId = Number(localStorage.getItem("userId"));
    const heartBtn = document.querySelector(`.heart-btn[data-moment-id="${momentId}"]`);

    const isLiked = hearts.some(h => Number(h.user?.id) === myUserId);

    heartBtn.innerText = isLiked ? "❤️" : "🤍";
    heartBtn.dataset.liked = isLiked ? "true" : "false";

    // Click to show popup
    countSpan.style.cursor = "pointer";
    countSpan.onclick = () => showHeartUsers(momentId);
}

async function toggleHeart(momentId, isLiked) {
    const mutation = isLiked
        ? `mutation($momentId: ID!) { deleteHeart(momentId: $momentId) }`
        : `mutation($momentId: ID!) { addHeart(momentId: $momentId) }`;

    try {
        const result = await graphqlRequest(mutation, { momentId });

        // Debug: xem response
        console.log("toggleHeart result:", result);

        // Nếu có lỗi GraphQL
        if (result.errors && result.errors.length > 0) {
            console.error("GraphQL error:", result.errors);
            return false;
        }

        // Lấy đúng value trả về
        const resValue = isLiked ? result.data?.deleteHeart : result.data?.addHeart;

        if (resValue === "ok") return true;

        console.error("Unexpected response:", resValue);
        return false;

    } catch (err) {
        console.error("toggleHeart error:", err);
        return false;
    }
}

async function showHeartUsers(momentId) {
    const hearts = await loadHearts(momentId);

    if (hearts.length === 0) {
        alert("Chưa có ai thích khoảnh khắc này.");
        return;
    }

    const usersHtml = hearts
        .map(h => `
            <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
                <img src="${h.user.avatarUrl || "/icon/default-avatar.png"}"
                     style="width:30px;height:30px;border-radius:50%;">
                <span>${h.user.userFullName}</span>
            </div>
        `)
        .join("");

    const popup = document.createElement("div");
    popup.innerHTML = `
        <div style="
            position:fixed; top:50%; left:50%; transform:translate(-50%, -50%);
            background:#fff; border-radius:10px; padding:20px;
            box-shadow:0 3px 20px rgba(0,0,0,0.25); z-index:2000;
            max-height:400px; overflow:auto;
        ">
            <h3 style="margin-top:0;">Người đã thích ❤️</h3>
            ${usersHtml}
            <button id="close-heart-popup" style="margin-top:12px;padding:6px 16px;">Đóng</button>
        </div>
    `;

    document.body.appendChild(popup);
    document.getElementById("close-heart-popup").onclick = () => popup.remove();
}

async function loadComments(momentId) {
    const query = `
      query GetComments($momentId: ID, $page: Int!, $size: Int!) {
        getComments(momentId: $momentId, page: $page, size: $size) {
          id
          comment
          commentDate
          user {
            userFullName
            avatarUrl
          }
        }
      }`;

    try {
        const result = await graphqlRequest(query, { momentId, page: 0, size: 10 });

        return result.data?.getComments || [];
    } catch (err) {
        console.error("Load comments error:", err);
        return [];
    }
}

async function sendComment(momentId, commentText) {
    const mutation = `
      mutation CreateComment($momentId: ID!, $request: CommentInput!) {
        createComment(momentId: $momentId, request: $request)
      }`;

    try {
        const result = await graphqlRequest(mutation, {
            momentId,
            request: { comment: commentText }
        });

        const response = result.data?.createComment;

        if (response && response.toLowerCase().includes("ok")) {
            await renderComments(momentId);
        } else {
            alert(response || "Lỗi khi gửi bình luận!");
        }
    } catch (err) {
        console.error("Send comment error:", err);
        alert("Không thể kết nối đến máy chủ!");
    }
}

async function renderComments(momentId) {
    const commentContainer = document.getElementById(`comments-${momentId}`);
    if (!commentContainer) return;

    const comments = await loadComments(momentId);
    const listDiv = commentContainer.querySelector(".comment-list");

    if (comments.length === 0) {
        listDiv.innerHTML = `<p style="color:#777;font-size:13px;">Chưa có bình luận nào.</p>`;
        return;
    }

    listDiv.innerHTML = comments.map(c => `
      <div style="display:flex;gap:8px;margin-bottom:6px;">
        <img src="${c.user.avatarUrl || '/icon/default-avatar.png'}"
             style="width:30px;height:30px;border-radius:50%;">
        <div>
          <strong>${c.user.userFullName}</strong>
          <p style="margin:0;">${c.comment}</p>
          <span style="font-size:12px;color:#666;">
            ${new Date(c.commentDate).toLocaleString()}
          </span>
        </div>
      </div>
    `).join("");
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

momentsContainer.addEventListener("click", async (e) => {
    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    const momentId = heartBtn.dataset.momentId;
    const isLiked = heartBtn.dataset.liked === "true";

    const ok = await toggleHeart(momentId, isLiked);

    if (ok) {
        const countSpan = document.getElementById(`heart-count-${momentId}`);
        let currentCount = Number(countSpan.innerText);

        if (isLiked) {
            heartBtn.innerText = "🤍";
            countSpan.innerText = currentCount - 1;
            heartBtn.dataset.liked = "false";
        } else {
            heartBtn.innerText = "❤️";
            countSpan.innerText = currentCount + 1;
            heartBtn.dataset.liked = "true";
        }
    } else {
        alert("Không thể cập nhật tim!");
    }
});

// Gắn sự kiện click cho button gửi comment
momentsContainer.addEventListener("click", async (e) => {
    const commentBtn = e.target.closest(".comment-send");
    if (!commentBtn) return;

    const momentId = commentBtn.dataset.momentId;
    const input = commentBtn.closest(".comment-form").querySelector(".comment-input");
    const commentText = input.value.trim();

    if (!commentText) {
        alert("Vui lòng nhập nội dung bình luận!");
        return;
    }

    commentBtn.disabled = true;
    commentBtn.innerText = "Đang gửi...";

    try {
        await sendComment(momentId, commentText);
        input.value = ""; // Clear input sau khi gửi
    } finally {
        commentBtn.disabled = false;
        commentBtn.innerText = "Gửi";
    }
});

fetchMoments();