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
    if (!accessToken) {
        alert("Vui lòng đăng nhập");
        return window.location.href = "/auth.html";
    }
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

function renderMoments(moments, append = false) {
    if (!append) momentsList.innerHTML = "";

    moments.forEach(moment => {
        const div = document.createElement("div");
        div.className = "moment";

        const editedText = moment.edited ? `<span class="moment-edited">(Đã chỉnh sửa)</span>` : "";

        div.innerHTML = `
          <div class="moment-header">
            <img class="moment-avatar" src="${moment.userAvatar || '/icon/default-avatar.png'}" alt="avatar">
            <div class="moment-header-info">
              <span class="moment-user">${moment.userFullName}</span>
              <div class="moment-meta">
                <span class="moment-time">${new Date(moment.createdAt).toLocaleString()}</span>
                ${moment.share ? `<span class="moment-share">· ${moment.share}</span>` : ""}
                ${editedText}
              </div>
            </div>
          </div>

          <div class="moment-content">${moment.content || ""}</div>
          <div class="moment-images">
            ${moment.imageUrls.map(url => `<img src="${url}" alt="moment image">`).join("")}
          </div>

          <div class="moment-actions">
            <button class="moment-btn edit-btn" data-id="${moment.id}" data-content="${moment.content}" data-share="${moment.share}">✏️</button>
            <button class="moment-btn delete-btn" data-id="${moment.id}">🗑</button>
          </div>

          <div class="moment-heart" style="margin-top:8px; display:flex; align-items:center; gap:10px;">
            <button class="heart-btn" data-moment-id="${moment.id}" style="font-size:20px; cursor:pointer;">🤍</button>
            <span class="heart-count" id="heart-count-${moment.id}" style="cursor:pointer">0</span>
          </div>

          <div class="moment-comments" id="comments-${moment.id}">
            <div class="comment-list"></div>
            <div class="comment-form">
              <input type="text" placeholder="Viết bình luận..." class="comment-input">
              <button class="comment-send" data-moment-id="${moment.id}">Gửi</button>
            </div>
          </div>
        `;
        momentsList.appendChild(div);

        // Load dữ liệu Heart & Comment
        renderHearts(moment.id);
        loadComments(moment.id);
    });
}

// --- Reset danh sách ---
function resetMoments() {
    currentPage = 0;
    endReached = false;
    loadMoments();
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
        const result = await graphqlRequest(query, { momentId, page: 0, size: 99 });
        return result.data?.getHeartsByMomentId || [];
    } catch (err) {
        console.error("Load hearts error:", err);
        return [];
    }
}

async function renderHearts(momentId) {
    const hearts = await loadHearts(momentId);

    const countSpan = document.getElementById(`heart-count-${momentId}`);
    countSpan.innerText = hearts.length;

    const myUserId = Number(localStorage.getItem("userId"));
    const heartBtn = document.querySelector(`.heart-btn[data-moment-id='${momentId}']`);

    const isLiked = hearts.some(h => Number(h.user.id) === myUserId);

    heartBtn.innerText = isLiked ? "❤️" : "🤍";
    heartBtn.dataset.liked = isLiked;

    countSpan.onclick = () => showHeartUsers(momentId);
}

async function toggleHeart(momentId, isLiked) {
    const mutation = isLiked
        ? `mutation ($momentId: ID!) { deleteHeart(momentId: $momentId) }`
        : `mutation ($momentId: ID!) { addHeart(momentId: $momentId) }`;

    const result = await graphqlRequest(mutation, { momentId });

    const response = isLiked
        ? result.data?.deleteHeart
        : result.data?.addHeart;

    if (!response) {
        alert("Lỗi cập nhật tim!");
        return false;
    }
    return true;
}

// Event delegate cho Heart button
momentsList.addEventListener("click", async (e) => {
    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    const momentId = heartBtn.dataset.momentId;
    const liked = heartBtn.dataset.liked === "true";

    const success = await toggleHeart(momentId, liked);
    if (success) renderHearts(momentId);
});

async function showHeartUsers(momentId) {
    const hearts = await loadHearts(momentId);
    if (hearts.length === 0) {
        alert("Chưa có ai thích bài viết này");
        return;
    }

    const usersHtml = hearts.map(h => `
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
            <img src="${h.user.avatarUrl || '/icon/default-avatar.png'}" 
                 style="width:30px;height:30px;border-radius:50%;">
            <span>${h.user.userFullName}</span>
        </div>
    `).join("");

    const popup = document.createElement("div");
    popup.innerHTML = `
        <div style="
            position:fixed; top:50%; left:50%; transform:translate(-50%, -50%);
            background:#fff; border:1px solid #ccc; border-radius:8px;
            padding:16px; max-height:400px; overflow:auto; z-index:9999;
            box-shadow:0 2px 12px rgba(0,0,0,0.2);
        ">
            <h3 style="margin-top:0;margin-bottom:10px;">Người thích</h3>
            ${usersHtml}
            <button id="close-heart-popup" 
                    style="margin-top:10px;padding:6px 12px;">Đóng</button>
        </div>
    `;
    document.body.appendChild(popup);

    document.getElementById("close-heart-popup").onclick = () => {
        document.body.removeChild(popup);
    };
}

// --- Infinite scroll ---
window.addEventListener("scroll", () => {
    const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 300;
    if (nearBottom) loadMoments();
});

// --- Modal chỉnh sửa ---
function showEditForm(momentId, oldContent, oldShare) {
    // Nếu form chỉnh sửa đã mở, xóa nó đi trước
    const existingForm = document.querySelector(".edit-inline-form");
    if (existingForm) existingForm.remove();

    // Tìm phần tử moment đang được chỉnh sửa
    const targetMoment = document.querySelector(`.moment-actions button[data-id="${momentId}"]`).closest(".moment");

    // Tạo form chỉnh sửa inline
    const editForm = document.createElement("div");
    editForm.className = "edit-inline-form";
    editForm.style.marginTop = "10px";
    editForm.style.background = "#f8f9fa";
    editForm.style.padding = "10px";
    editForm.style.borderRadius = "8px";
    editForm.innerHTML = `
        <textarea id="editContent" rows="3" style="width:100%;padding:8px;border-radius:6px;border:1px solid #ccc;">${oldContent || ""}</textarea>
        <select id="editShare" style="margin-top:8px;width:100%;padding:8px;border-radius:6px;border:1px solid #ccc;">
            <option value="PUBLIC" ${oldShare === "PUBLIC" ? "selected" : ""}>Công khai</option>
            <option value="FOLLOWER" ${oldShare === "FOLLOWER" ? "selected" : ""}>Người theo dõi</option>
            <option value="PRIVATE" ${oldShare === "PRIVATE" ? "selected" : ""}>Chỉ mình tôi</option>
        </select>
        <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:8px;">
            <button id="saveEdit" style="background:#007bff;color:#fff;padding:6px 12px;border:none;border-radius:6px;cursor:pointer;">Lưu</button>
            <button id="cancelEdit" style="background:#ccc;color:#333;padding:6px 12px;border:none;border-radius:6px;cursor:pointer;">Hủy</button>
        </div>
    `;

    // Gắn form ngay sau moment đang chọn
    targetMoment.appendChild(editForm);

    // Gán sự kiện
    editForm.querySelector("#saveEdit").onclick = () => {
        const newContent = editForm.querySelector("#editContent").value.trim();
        const newShare = editForm.querySelector("#editShare").value;
        if (!newContent) {
            alert("Nội dung không được để trống!");
            return;
        }

        updateMoment({ momentId: Number(momentId), content: newContent, share: newShare });
        editForm.remove();
    };

    editForm.querySelector("#cancelEdit").onclick = () => {
        editForm.remove();
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

momentsList.addEventListener("click", (e) => {
    const editBtn = e.target.closest(".edit-btn");
    const deleteBtn = e.target.closest(".delete-btn");

    if (editBtn) {
        showEditForm(editBtn.dataset.id, editBtn.dataset.content, editBtn.dataset.share);
    }
    if (deleteBtn) {
        deleteMoment(deleteBtn.dataset.id);
    }
});

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

async function loadComments(momentId) {
    const query = `
      query GetComments($momentId: ID, $page: Int!, $size: Int!) {
        getComments(momentId: $momentId, page: $page, size: $size) {
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
    `;

    const result = await graphqlRequest(query, { momentId, page: 0, size: 50 });
    const comments = result.data?.getComments || [];

    const listDiv = document.querySelector(`#comments-${momentId} .comment-list`);
    listDiv.innerHTML = comments
        .map(c => `
            <div class="comment-item">
                <img src="${c.user.avatarUrl || '/icon/default-avatar.png'}" class="comment-avatar">
                <div class="comment-body">
                    <div class="comment-author">${c.user.userFullName}</div>
                    <div class="comment-text">${c.comment}</div>
                    <div class="comment-date">${formatCommentDate(c.commentDate)}</div>
                </div>
            </div>
        `)
        .join("");
}

function formatCommentDate(dateStr) {
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0'); // tháng từ 0-11
    const year = d.getFullYear();
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');

    return `${day}/${month}/${year} ${hours}:${minutes}`;
}


async function createComment(momentId, text) {
    const mutation = `
      mutation CreateComment($momentId: ID!, $request: CommentInput!) {
        createComment(momentId: $momentId, request: $request)
      }
    `;

    const variables = {
        momentId,
        request: { comment: text }
    };

    const result = await graphqlRequest(mutation, variables);
    return result.data?.createComment;
}

momentsList.addEventListener("click", async (e) => {
    const btn = e.target.closest(".comment-send");
    if (!btn) return;

    const momentId = btn.dataset.momentId;
    const input = btn.previousElementSibling;
    const text = input.value.trim();
    if (!text) return;

    await createComment(momentId, text);
    input.value = "";
    loadComments(momentId);
});

loadMoments();
