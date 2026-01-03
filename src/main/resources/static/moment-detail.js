const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const GRAPHQL_URL = `${URL_BASE}/graphql`;

async function graphqlRequest(query, variables = {}) {
    const res = await fetch(GRAPHQL_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${accessToken}`
        },
        body: JSON.stringify({query, variables})
    });
    return await res.json();
}

/* ================= Query ================= */

const QUERY_MOMENT_DETAIL = `
query ($momentId: ID!) {
  momentDetail(momentId: $momentId) {
    id
    actorId
    isHeart
    isFollowing
    mySelf
    actor {
      userFullName
      avatarUrl
    }
    moment {
      content
      createdAt
      share
      imgUrls
      comments {
        comment
        commentDate
        user {
          id
          userFullName
          avatarUrl
        }
      }
      hearts {
        user {
          id
          userFullName
          avatarUrl
        }
      }
    }
  }
}
`;

/* ================= Load ================= */

const container = document.getElementById("moment-detail");
const params = new URLSearchParams(window.location.search);
const momentId = params.get("id");

if (momentId) {
    loadMomentDetail(momentId);
}

async function loadMomentDetail(momentId) {
    const res = await graphqlRequest(QUERY_MOMENT_DETAIL, {
        momentId   // ID → không cần ép Number
    });

    if (res.errors) {
        console.error(res.errors);
        container.innerHTML = `<p>Không thể tải bài viết</p>`;
        return;
    }

    renderMomentDetail(res.data.momentDetail);
}

function renderMomentDetail(data) {
    const {actor, actorId, isHeart, isFollowing, mySelf, moment} = data;

    container.innerHTML = `
        <div class="moment-card">

            <!-- Header -->
            <div class="moment-header">
                <div class="actor-info clickable" onclick="goToUser(${actorId})">
                    <img class="avatar"src="${actor.avatarUrl || '/icon/default-avatar.png'}"  alt=""/>
                    <div>
                        <div class="actor-name">${actor.userFullName}</div>
                        <div class="moment-meta">${formatDate(moment.createdAt)} · ${moment.share}</div>
                    </div>
                </div>

                ${
        mySelf
            ? ""
            : `<button 
                   class="follow-btn ${isFollowing ? "following" : ""}"
                   onclick="toggleFollow(${actorId}, ${isFollowing})">
                   ${isFollowing ? "Đang theo dõi" : "Theo dõi"}
               </button>`
    }
            </div>

            <div class="moment-content">${moment.content}</div>

            ${renderImages(moment.imgUrls)}

            <div class="moment-actions">
                <button style="border: none; background-color: #ffffff; cursor: pointer"
                  class="heart-btn ${isHeart ? "active" : ""}"
                  onclick="toggleHeart(${momentId}, ${isHeart})">
                    ❤️ ${moment.hearts.length}
                </button>

                <span>💬 ${moment.comments.length}</span>
            </div>

            ${renderHeartUsers(moment.hearts)}
            
                        <!-- Add Comment -->
            <div class="comment-input">
                <input
                    type="text"
                    id="commentInput"
                    placeholder="Viết bình luận..."
                    onkeydown="handleCommentKey(event)"
                />
                <button onclick="submitComment()">Gửi</button>
            </div>

            <div class="comment-list">
                <h4>Bình luận</h4>
                ${
                moment.comments.length
                ? moment.comments.map(renderComment).join("")
            : `<p class="empty">Chưa có bình luận</p>`
    }
            </div>
        </div>
    `;
}

async function toggleFollow(userId, isFollowing) {
    try {
        let res;

        if (!isFollowing) {
            // FOLLOW
            res = await fetch(`${URL_BASE}/follow?userId=${userId}`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${accessToken}`
                }
            });
        } else {
            // UNFOLLOW
            res = await fetch(
                `${URL_BASE}/follow?userId=${userId}&action=UNFOLLOW`,
                {
                    method: "PUT",
                    headers: {
                        "Authorization": `Bearer ${accessToken}`
                    }
                }
            );
        }

        const json = await res.json();
        if (!json.success) {
            alert(json.message);
            return;
        }

        loadMomentDetail(momentId);

    } catch (e) {
        console.error(e);
        alert("Có lỗi xảy ra");
    }
}

window.toggleFollow = toggleFollow

function renderImages(images = []) {
    if (!images.length) return "";

    return `
        <div class="moment-images grid-${Math.min(images.length, 3)}">
            ${images.map(url => `
                <img src="${url}"  alt=""/>
            `).join("")}
        </div>
    `;
}

function renderHeartUsers(hearts = []) {
    if (!hearts.length) return "";

    const preview = hearts.slice(0, 5);

    return `
        <div class="heart-users">
            ${preview.map(h => `
                <img class="avatar-sm clickable"
                     title="${h.user.userFullName}"
                     onclick="goToUser(${h.user.id})"
                     src="${h.user.avatarUrl || '/icon/default-avatar.png'}" />
            `).join("")}
            ${hearts.length > 5 ? `<span>+${hearts.length - 5}</span>` : ""}
        </div>
    `;
}

function renderComment(comment) {
    return `
        <div class="comment-item">
            <img class="avatar-sm clickable"
                 onclick="goToUser(${comment.user.id})"
                 src="${comment.user.avatarUrl || '/icon/default-avatar.png'}" />

            <div class="comment-body">
                <div class="comment-header">
                    <strong class="clickable"
                            onclick="goToUser(${comment.user.id})">
                        ${comment.user.userFullName}
                    </strong>
                    <small>${formatDate(comment.commentDate)}</small>
                </div>
                <div class="comment-text">${comment.comment}</div>
            </div>
        </div>
    `;
}

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString();
}

const MUTATION_CREATE_COMMENT = `
mutation ($momentId: ID!, $comment: String!) {
  createComment(
    momentId: $momentId,
    request: { comment: $comment }
  ) {
    success
    message
  }
}
`;

async function submitComment() {
    const input = document.getElementById("commentInput");
    const comment = input.value.trim();

    if (!comment) return;

    try {
        const res = await graphqlRequest(MUTATION_CREATE_COMMENT, {
            momentId,
            comment
        });

        if (res.errors || !res.data.createComment.success) {
            alert(res?.data?.createComment?.message || "Không thể gửi bình luận");
            return;
        }

        input.value = "";
        loadMomentDetail(momentId); // reload để sync comments

    } catch (e) {
        console.error(e);
        alert("Có lỗi xảy ra");
    }
}
window.submitComment=submitComment

function handleCommentKey(e) {
    if (e.key === "Enter") {
        submitComment();
    }
}
window.handleCommentKey=handleCommentKey

const MUTATION_ADD_HEART = `
mutation ($momentId: ID!) {
  addHeart(momentId: $momentId) {
    success
    message
  }
}
`;

const MUTATION_DELETE_HEART = `
mutation ($momentId: ID!) {
  deleteHeart(momentId: $momentId) {
    success
    message
  }
}
`;

async function toggleHeart(momentId, isHeart) {
    try {
        const res = await graphqlRequest(
            isHeart ? MUTATION_DELETE_HEART : MUTATION_ADD_HEART,
            { momentId }
        );

        const data = isHeart
            ? res.data.deleteHeart
            : res.data.addHeart;

        if (!data.success) {
            alert(data.message);
            return;
        }
        loadMomentDetail(momentId);

    } catch (e) {
        console.error(e);
        alert("Không thể thực hiện thao tác");
    }
}
window.toggleHeart = toggleHeart;

function goToUser(userId) {
    if (!userId) return;
    window.location.href = `user.html?id=${userId}`;
}
window.goToUser = goToUser;