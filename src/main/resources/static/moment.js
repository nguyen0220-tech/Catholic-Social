const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const form = document.getElementById("momentForm");
const preview = document.getElementById("preview");
const messageBox = document.getElementById("messageBox");
const momentsList = document.getElementById("momentsList");
const GRAPHQL_URL = `${URL_BASE}/graphql`;
const currentUserId = Number(localStorage.getItem("userId"));


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

            <div class="moment-heart" style="margin-top:8px;">
              <div style="display:flex;align-items:center;gap:10px;">
                <button class="heart-btn"
                        data-moment-id="${moment.id}"
                        style="font-size:20px; cursor:pointer;">🤍</button>
                <span class="heart-count"
                      id="heart-count-${moment.id}"
                      style="cursor:pointer">0</span>
              </div>
            
              <div class="heart-users"
                   id="heart-users-${moment.id}"
                   style="display:none;margin-top:6px;background:#fff;
                          border:1px solid #ddd;border-radius:6px;padding:6px;">
              </div>
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
        const commentList = div.querySelector(".comment-list");
        loadComments(moment.id, commentList);
        renderHearts(moment.id);

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

async function loadComments(momentId, container) {
    const res = await graphqlRequest(GET_COMMENTS, {
        momentId,
        page: 0,
        size: 5
    });

    const comments = res.data?.getComments?.data || [];
    container.innerHTML = "";

    comments.forEach(c => {
        const div = document.createElement("div");
        div.className = "comment-item";
        div.innerHTML = `
          <img src="${c.user.avatarUrl || '/icon/default-avatar.png'}" class="comment-avatar">
          <div>
            <b>${c.user.userFullName}</b>
            <p>${c.comment}</p>
            <small>${new Date(c.commentDate).toLocaleString()}</small>
          </div>
        `;
        container.appendChild(div);
    });
}

momentsList.addEventListener("click", async (e) => {
    const sendBtn = e.target.closest(".comment-send");
    if (!sendBtn) return;

    const momentId = sendBtn.dataset.momentId;
    const wrapper = sendBtn.closest(".moment-comments");
    const input = wrapper.querySelector(".comment-input");
    const commentList = wrapper.querySelector(".comment-list");

    const text = input.value.trim();
    if (!text) return;

    const res = await graphqlRequest(CREATE_COMMENT, {
        momentId,
        comment: text
    });

    if (res.data?.createComment?.success) {
        input.value = "";
        loadComments(momentId, commentList); // reload comment
    } else {
        alert(res.data?.createComment?.message || "Lỗi gửi comment");
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

    renderHeartUsers(momentId, hearts);
}

function renderHeartUsers(momentId, hearts) {
    const box = document.getElementById(`heart-users-${momentId}`);
    if (!box) return;

    if (hearts.length === 0) {
        box.innerHTML = "<i style='color:#777'>Chưa có ai thả tim</i>";
        return;
    }

    box.innerHTML = hearts.map(h => `
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
            <img src="${
        h.user.avatarUrl
            ? (h.user.avatarUrl.startsWith("http")
                ? h.user.avatarUrl
                : URL_BASE + h.user.avatarUrl)
            : "/icon/default-avatar.png"
    }"
            style="width:28px;height:28px;border-radius:50%;object-fit:cover;">
            <span style="font-size:14px;">${h.user.userFullName}</span>
        </div>
    `).join("");
}

momentsList.addEventListener("click", async (e) => {
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

momentsList.addEventListener("click", (e) => {
    const countEl = e.target.closest(".heart-count");
    if (!countEl) return;

    const momentId = countEl.id.replace("heart-count-", "");
    const box = document.getElementById(`heart-users-${momentId}`);
    if (!box) return;

    box.style.display = box.style.display === "none" ? "block" : "none";
});


loadMoments();
