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

const params = new URLSearchParams(window.location.search);
const paramUserId = params.get("id");
const currentUserId = localStorage.getItem("userId");

const userId = paramUserId ? Number(paramUserId) : Number(currentUserId);

const PROFILE_QUERY = `
query ($userId: ID!) {
  profile(userId: $userId) {
    id
    isFollowing
    isBlocked
    user {
      fullName
      avatarUrl
    }
    moments(page: 0, size: 10) {
      content {
        id
        content
        createdAt
        share
        imgUrls
        comments {
          id
          comment
          commentDate
          user {
            id
            userFullName
            avatarUrl
          }
        }
        hearts {
          id
          user {
            id
            userFullName
            avatarUrl
          }
        }
      }
    }
  }
}
`;

async function loadProfile() {
    const res = await graphqlRequest(PROFILE_QUERY, {userId});
    const profile = res.data.profile;

    renderProfile(profile);
    renderMoments(profile.moments.content);
}

function renderProfile(profile) {
    const isMe = userId === Number(currentUserId);

    let followBtn = "";
    let blockBtn = "";

    if (!isMe) {
        if (profile.isBlocked) {
            blockBtn = `
                <button class="btn unblock" onclick="unblockUser(${userId})">
                    Unblock
                </button>
            `;
        } else {
            blockBtn = `
                <button class="btn block" onclick="blockUser(${userId})">
                    Block
                </button>
            `;
        }

        if (!profile.isBlocked) {
            if (profile.isFollowing) {
                followBtn = `
                    <button class="btn unfollow" onclick="unfollowUser(${userId})">
                        Unfollow
                    </button>
                `;
            } else {
                followBtn = `
                    <button class="btn follow" onclick="followUser(${userId})">
                        Follow
                    </button>
                `;
            }
        }
    }

    document.getElementById("profile").innerHTML = `
        <div class="profile-header">
            <img src="${profile.user.avatarUrl}">
            <div>
                <h2>${profile.user.fullName}</h2>
                <div class="profile-actions">
                    ${followBtn}
                    ${blockBtn}
                </div>
            </div>
        </div>
    `;
}

function renderMoments(moments) {
    const container = document.getElementById("moments");
    container.innerHTML = "";

    const myId = Number(localStorage.getItem("userId"));

    moments.forEach(m => {
        const isHearted = m.hearts.some(h => Number(h.user.id) === myId);
        const heartIcon = isHearted ? "❤️" : "🤍";

        container.innerHTML += `
            <div class="moment">
                <p>${m.content}</p>

                ${m.imgUrls.length > 0 ? `
                <div class="moment-images">
                    ${m.imgUrls.map(url => `<img src="${url}">`).join("")}
                </div>` : ""}

                <div class="createdAt">
                    ${new Date(m.createdAt).toLocaleString()} · ${m.share}
                </div>

                <div class="hearts-section">
                    <span class="heart-btn" 
                          style="cursor:pointer; font-size: 1.2rem;" 
                          onclick="toggleHeart('${m.id}', ${isHearted})">
                        ${heartIcon} ${m.hearts.length}
                    </span>
                    
                    <div class="hearts-list" style="display: flex; gap: 5px; margin-top: 5px;">
                        ${m.hearts.map(h => `
                            <div class="heart-user" onclick="goToProfile(${h.user.id})" title="${h.user.userFullName}">
                                <img src="${h.user.avatarUrl}" style="width: 20px; height: 20px; border-radius: 50%;">
                            </div>
                        `).join("")}
                    </div>
                </div>

                <div class="comments">
                    💬 ${m.comments.length}
                    ${m.comments.map(c => `
                        <div class="comment">
                            <img src="${c.user.avatarUrl}" onclick="goToProfile(${c.user.id})">
                            <b onclick="goToProfile(${c.user.id})">${c.user.userFullName}</b>
                            <span>${c.comment}</span>
                        </div>
                    `).join("")}
                </div>

                <div class="add-comment-section" style="margin-top: 10px; display: flex; gap: 5px;">
                    <input type="text" 
                           id="comment-input-${m.id}" 
                           placeholder="Viết bình luận..." 
                           style="flex: 1; padding: 5px;"
                           onkeydown="if(event.key === 'Enter') postComment('${m.id}')"
                    >
                    <button onclick="postComment('${m.id}')" style="padding: 5px 10px; cursor: pointer; background-color: #EE82EE; color: #00fb00">
                        Gửi
                    </button>
                </div>
            </div>
        `;
    });
}

function goToProfile(userId) {
    window.location.href = `/user.html?id=${userId}`;
}

async function followUser(userId) {
    await fetch(`/follow?userId=${userId}`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });
    loadProfile();
}

window.followUser = followUser

async function unfollowUser(userId) {
    await fetch(`/follow?userId=${userId}&action=UNFOLLOW`, {
        method: "PUT",
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });
    loadProfile();
}

window.unfollowUser = unfollowUser;

async function blockUser(userId) {
    await fetch(`/follow/block?userId=${userId}`, {
        method: "PUT",
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });
    loadProfile();
}

window.blockUser = blockUser

async function unblockUser(userId) {
    await fetch(`/follow?userId=${userId}&action=UNBLOCK`, {
        method: "PUT",
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });
    loadProfile();
}

window.unblockUser = unblockUser

window.goToProfile = goToProfile;

const CREATE_COMMENT_MUTATION = `
mutation ($momentId: ID!, $request: CommentInput!) {
  createComment(momentId: $momentId, request: $request) {
    message
  }
}
`;

async function postComment(momentId) {
    const inputElement = document.getElementById(`comment-input-${momentId}`);
    const commentText = inputElement.value.trim();

    if (!commentText) {
        alert("Vui lòng nhập nội dung bình luận!");
        return;
    }

    try {
        const variables = {
            momentId: momentId,
            request: {
                comment: commentText
            }
        };

        const res = await graphqlRequest(CREATE_COMMENT_MUTATION, variables);

        if (res.errors) {
            console.error(res.errors);
            alert("Có lỗi xảy ra khi bình luận.");
        } else {
            inputElement.value = "";
            loadProfile();
        }
    } catch (error) {
        console.error("Error posting comment:", error);
    }
}

window.postComment = postComment;

const ADD_HEART_MUTATION = `
mutation ($momentId: ID!) {
  addHeart(momentId: $momentId) {
    message
  }
}
`;

const DELETE_HEART_MUTATION = `
mutation ($momentId: ID!) {
  deleteHeart(momentId: $momentId) {
    message
  }
}
`;

async function toggleHeart(momentId, isHearted) {
    const query = isHearted ? DELETE_HEART_MUTATION : ADD_HEART_MUTATION;

    try {
        const res = await graphqlRequest(query, { momentId });
        if (res.errors) {
            console.error(res.errors);
        } else {
            loadProfile();
        }
    } catch (error) {
        console.error("Error toggling heart:", error);
    }
}

window.toggleHeart = toggleHeart;

loadProfile();
