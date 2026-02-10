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
    numOfMoments
    numOfFollowers
    numOfFollowing
    isFollowing
    isBlocked
    bio
    mutualFollowers{
      userId
      user{
        userFullName
        avatarUrl
      }
    }
    user {
      fullName
      avatarUrl
    }
    hasRoom{
      chatRoomId
    }
    moments(page: 0, size: 10) {
      content {
        id
        content
        createdAt
        share
        imgUrls
        saved
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

const FOLLOWERS_QUERY = `
query ($userId: ID!, $page: Int!, $size: Int!) {
  profile(userId: $userId) {
    followers(page: $page, size: $size) {
      data {
        userId
        user {
          userFullName
          avatarUrl
        }
        isFollowed
      }
      pageInfo {
        page
        size
        hasNext
      }
    }
  }
}
`;

const FOLLOWING_QUERY = `
query ($userId: ID!, $page: Int!, $size: Int!) {
  profile(userId: $userId) {
    following(page: $page, size: $size) {
      data {
        userId
        user {
          userFullName
          avatarUrl
        }
        isFollowed
      }
      pageInfo {
        page
        size
        hasNext
      }
    }
  }
}
`;

const ACCOUNT_INFO_QUERY = `
query ($userId: ID!) {
  profile(userId: $userId) {
    createdAt
    intro {
      id
      url
    }
  }
}
`;


const CREATE_SAVED_MUTATION = `
mutation ($momentId: ID!) {
  createSaved(momentId: $momentId) {
    success
    message
  }
}
`;

const DELETE_SAVED_MUTATION = `
mutation ($momentId: ID!) {
  deleteSaved(momentId: $momentId) {
    success
    message
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

    let messageBtn = "";
    let followBtn = "";
    let blockBtn = "";
    let infoBtn = "";

    if (!isMe) {
        if (!profile.isBlocked) {
            window.currentProfileData = profile;
            messageBtn = `
                <button class="btn message" onclick="handleMessageAction()">
                    Nhắn tin
                </button>
            `;
        }

        if (profile.isBlocked) {
            blockBtn = `<button class="btn unblock" onclick="unblockUser(${userId})">Unblock</button>`;
        } else {
            blockBtn = `<button class="btn block" onclick="blockUser(${userId})">Block</button>`;
            if (profile.isFollowing) {
                followBtn = `<button class="btn unfollow" onclick="unfollowUser(${userId})">Unfollow</button>`;
            } else {
                followBtn = `<button class="btn follow" onclick="followUser(${userId})">Follow</button>`;
            }
        }
        infoBtn = `<button class="btn info" onclick="showAccountInfo(${userId})">Giới thiệu tài khoản này</button>`;
    }

    document.getElementById("profile").innerHTML = `
    <div class="profile-header">
        <img src="${profile.user.avatarUrl}" class="profile-avatar" alt="">
        <div class="profile-info">
            <h2>${profile.user.fullName}</h2>
            ${profile.bio ? `<p class="profile-bio">${escapeHtml(profile.bio)}</p>` : ""}
            ${!isMe ? renderMutualFollowers(profile) : ""}
            <div class="profile-meta-row">
                <div class="profile-stats">
                    <div class="stat"><b>${profile.numOfMoments}</b><span>Moments</span></div>
                    <div class="stat" onclick="goToFollowers(${userId})"><b>${profile.numOfFollowers}</b><span>Followers</span></div>
                    <div class="stat" onclick="goToFollowing(${userId})"><b>${profile.numOfFollowing}</b><span>Following</span></div>
                </div>
                <div class="profile-actions">
                    ${messageBtn} 
                    ${followBtn}
                    ${blockBtn}
                    ${infoBtn}
                </div>
            </div>
        </div>
    </div>
    `;
}

function handleMessageAction() {
    const profile = window.currentProfileData;

    if (!profile) return;

    if (profile.hasRoom && profile.hasRoom.chatRoomId) {
        window.location.href = `message.html?chatRoomId=${profile.hasRoom.chatRoomId}`;
    } else {
        document.getElementById("dmRecipientId").value = profile.id;
        document.getElementById("dmRecipientName").innerText = profile.user.fullName;
        document.getElementById("directMessageModal").classList.remove("hidden");
    }
}

function closeDirectMessageModal() {
    document.getElementById("directMessageModal").classList.add("hidden");
    document.getElementById("dmText").value = "";
    document.getElementById("dmMedias").value = "";
}

async function submitDirectMessage() {
    const recipientId = document.getElementById("dmRecipientId").value;
    const message = document.getElementById("dmText").value.trim();
    const mediaFiles = document.getElementById("dmMedias").files;
    const btn = document.getElementById("btnSendDM");

    if (!message && mediaFiles.length === 0) {
        alert("Vui lòng nhập nội dung hoặc chọn ảnh!");
        return;
    }

    btn.disabled = true;
    btn.innerText = "Đang gửi...";

    const formData = new FormData();
    formData.append("recipientId", recipientId);
    formData.append("message", message);
    for (let i = 0; i < mediaFiles.length; i++) {
        formData.append("medias", mediaFiles[i]);
    }

    try {
        const response = await fetch(`${URL_BASE}/chat/send-direct`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            },
            body: formData
        });

        const result = await response.json();

        if (response.ok) {
            window.location.href = `message.html?chatRoomId=${result.data.chatRoomId}`;
        } else {
            alert("Lỗi: " + (result.message || "Không thể gửi tin nhắn"));
        }
    } catch (error) {
        console.error("Error sending direct message:", error);
    } finally {
        btn.disabled = false;
        btn.innerText = "Gửi";
        closeDirectMessageModal();
    }
}

window.handleMessageAction = handleMessageAction;
window.closeDirectMessageModal = closeDirectMessageModal;
window.submitDirectMessage = submitDirectMessage;

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function renderMutualFollowers(profile) {
    const list = profile.mutualFollowers || [];

    if (list.length === 0) return "";

    // Render avatars (tối đa 2 như BE trả)
    const avatars = list.slice(0, 2).map((mf, index) => `
        <img src="${mf.user.avatarUrl}"
             class="mutual-avatar"
             onclick="goToProfile(${mf.userId})"
             style="
                width: 24px;
                height: 24px;
                border-radius: 50%;
                border: 2px solid #fff;
                position: relative;
                left: ${index * -8}px;
                cursor: pointer;
             "
             alt="">
    `).join("");

    // Render names
    let text = "";
    if (list.length === 1) {
        text = `
            Followed by
            <span class="mutual-name" onclick="goToProfile(${list[0].userId})">
                ${list[0].user.userFullName}
            </span>
        `;
    } else {
        text = `
            Followed by
            <span class="mutual-name" onclick="goToProfile(${list[0].userId})">
                ${list[0].user.userFullName}
            </span>
            and
            <span class="mutual-name" onclick="goToProfile(${list[1].userId})">
                ${list[1].user.userFullName}
            </span>
        `;
    }

    const hasMore = list.length >= 3;

    return `
        <div class="mutual-followers"
             style="display:flex; align-items:center; gap:6px; margin-top:6px; font-size:0.9rem; color:#666;">
            
            <div class="mutual-avatars" style="display:flex; padding-left:8px;">
                ${avatars}
            </div>

            <div class="mutual-text">
                ${text}
                ${hasMore ? `
                    <span class="mutual-more"
                          onclick="goToFollowers(${profile.id})"
                          style="font-weight:600; cursor:pointer;">
                        · more
                    </span>
                ` : ""}
            </div>
        </div>
    `;
}

function goToFollowers(userId) {
    openFollowModal("Followers");
    loadFollowers(userId);
}

window.goToFollowers = goToFollowers

function goToFollowing(userId) {
    openFollowModal("Following");
    loadFollowing(userId);
}

window.goToFollowing = goToFollowing

async function loadFollowers(userId, page = 0, size = 10) {
    const res = await graphqlRequest(FOLLOWERS_QUERY, {userId, page, size});
    renderFollowList(res.data.profile.followers.data);
}

async function loadFollowing(userId, page = 0, size = 10) {
    const res = await graphqlRequest(FOLLOWING_QUERY, {userId, page, size});
    renderFollowList(res.data.profile.following.data);
}

function renderFollowList(list) {
    const container = document.getElementById("followList");
    container.innerHTML = "";

    const myId = Number(currentUserId);

    list.forEach(u => {
        const isMe = Number(u.userId) === myId;

        container.innerHTML += `
          <div class="follow-item" style="display:flex;align-items:center;gap:10px;margin-bottom:10px;">
            <img src="${u.user.avatarUrl}" 
                 style="width:40px;height:40px;border-radius:50%;cursor:pointer;"
                 onclick="goToProfile(${u.userId})" alt="">

            <span style="flex:1;cursor:pointer;"
                  onclick="goToProfile(${u.userId})">
                ${u.user.userFullName}
            </span>

            ${!isMe ? `
                <button onclick="toggleFollow(${u.userId}, ${u.isFollowed})">
                    ${u.isFollowed ? "Unfollow" : "Follow"}
                </button>
            ` : ""}
          </div>
        `;
    });
}

function openFollowModal(title) {
    document.getElementById("followModalTitle").innerText = title;
    document.getElementById("followModal").classList.remove("hidden");
}

function closeFollowModal() {
    document.getElementById("followModal").classList.add("hidden");
}

window.closeFollowModal = closeFollowModal

async function toggleFollow(userId, isFollowed) {
    if (isFollowed) {
        await unfollowUser(userId);
    } else {
        await followUser(userId);
    }
}

window.toggleFollow = toggleFollow

async function showAccountInfo(userId) {
    try {
        const res = await graphqlRequest(ACCOUNT_INFO_QUERY, { userId });

        if (res.errors || !res.data.profile) {
            console.error(res.errors);
            alert("Không thể lấy thông tin tài khoản");
            return;
        }

        const { createdAt, intro } = res.data.profile;

        const createdDate = new Date(createdAt).toLocaleDateString("vi-VN", {
            year: "numeric",
            month: "long",
            day: "numeric"
        });

        openAccountInfoModal(createdDate, intro);

    } catch (e) {
        console.error("showAccountInfo error", e);
    }
}

window.showAccountInfo = showAccountInfo;

function openAccountInfoModal(createdDate, intro) {
    document.getElementById("accountCreatedAt").innerText = createdDate;

    const introContainer = document.getElementById("accountIntro");

    if (intro && intro.url) {
        introContainer.innerHTML = `
            <video controls
                   style="width:100%; max-height:400px; border-radius:8px;">
                <source src="${intro.url}">
                Trình duyệt của bạn không hỗ trợ video.
            </video>
        `;
    } else {
        introContainer.innerHTML = `
            <p style="color:#777; font-style:italic;">
                Tài khoản này chưa có intro
            </p>
        `;
    }

    document.getElementById("accountInfoModal").classList.remove("hidden");
}

function closeAccountInfoModal() {
    document.getElementById("accountInfoModal").classList.add("hidden");
}

window.closeAccountInfoModal = closeAccountInfoModal;

function renderMoments(moments) {
    const container = document.getElementById("moments");
    container.innerHTML = "";

    const myId = Number(localStorage.getItem("userId"));

    moments.forEach(m => {
        const isHearted = m.hearts.some(h => Number(h.user.id) === myId);
        const heartIcon = isHearted ? "❤️" : "🤍";
        const saveIcon = m.saved ? "🔖" : "📑";

        container.innerHTML += `
            <div class="moment">
                <p>${m.content}</p>

                ${m.imgUrls.length > 0 ? `
                <div class="moment-images">
                    ${m.imgUrls.map(url => `<img src="${url}" alt="">`).join("")}
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
                
                    <span class="save-btn"
                          style="cursor:pointer; font-size: 1.2rem; margin-left: 10px;"
                          onclick="toggleSaved('${m.id}', ${m.saved})"
                          title="${m.saved ? 'Bỏ lưu' : 'Lưu'}">
                        ${saveIcon}
                    </span>
                
                    <div class="hearts-list" style="display: flex; gap: 5px; margin-top: 5px;">
                        ${m.hearts.map(h => `
                            <div class="heart-user" onclick="goToProfile(${h.user.id})" title="${h.user.userFullName}">
                                <img src="${h.user.avatarUrl}" style="width: 20px; height: 20px; border-radius: 50%;" alt="">
                            </div>
                        `).join("")}
                    </div>
                </div>

                <div class="comments">
                    💬 ${m.comments.length}
                    ${m.comments.map(c => `
                        <div class="comment">
                            <img src="${c.user.avatarUrl}" onclick="goToProfile(${c.user.id})" alt="">
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
        const res = await graphqlRequest(query, {momentId});
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

async function toggleSaved(momentId, isSaved) {
    const query = isSaved
        ? DELETE_SAVED_MUTATION
        : CREATE_SAVED_MUTATION;

    try {
        const res = await graphqlRequest(query, {momentId});

        if (res.errors) {
            console.error(res.errors);
            alert("Có lỗi khi lưu moment");
        } else {
            loadProfile(); // reload lại list để sync saved
        }
    } catch (error) {
        console.error("Error toggling saved:", error);
    }
}

window.toggleSaved = toggleSaved;

loadProfile();
