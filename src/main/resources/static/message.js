const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const GRAPHQL_URL = `${URL_BASE}/graphql`;

document.getElementById("addMemberLoading").style.display = "none";
document.getElementById("loading").style.display = "none";

const urlParams = new URLSearchParams(window.location.search);
const chatRoomId = urlParams.get("chatRoomId");

let currentPage = 0;
const pageSize = 10;
let hasNext = true;
let isLoading = false;

if (!chatRoomId) {
    alert("Thiếu chatRoomId");
}

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

const MESSAGES_QUERY = `
query ($chatRoomId: Int!, $page: Int!, $size: Int!) {
  messages(chatRoomId: $chatRoomId, page: $page, size: $size) {
    data {
      id
      text
      createdAt
      messageMedias
      user {
        id
        userFullName
        avatarUrl
      }
      isMine
    }
    pageInfo {
      page
      size
      hasNext
    }
  }
}
`;

async function loadMessages() {
    if (!hasNext || isLoading) return;

    isLoading = true;
    document.getElementById("loading").style.display = "block";

    const result = await graphqlRequest(MESSAGES_QUERY, {
        chatRoomId: Number(chatRoomId),
        page: currentPage,
        size: pageSize
    });

    if (result.errors) {
        console.error(result.errors);
        isLoading = false;
        document.getElementById("loading").style.display = "none";
        return;
    }

    const response = result.data.messages;
    const messages = response.data;
    hasNext = response.pageInfo.hasNext;

    const container = document.getElementById("messagesContainer");
    messages.forEach(msg => {
        const div = document.createElement("div");
        div.className = msg.isMine ? "message mine" : "message other";

        div.innerHTML = `
        ${!msg.isMine ? `
            <img class="avatar" 
                 src="${msg.user.avatarUrl || '/icon/default-avatar.png'}" 
                 alt="" 
                 style="cursor:pointer;" 
                 onclick="window.location.href='user.html?id=${msg.user.id}'">
        ` : ""}

        <div class="content">
            <div class="bubble">
                ${msg.text ? `<div class="message-text">${msg.text}</div>` : ""}
                ${renderMessageMedias(msg.messageMedias)}
            </div>
            <div class="time">${formatTime(msg.createdAt)}</div>
        </div>
    `;

        container.appendChild(div);
    });

    currentPage++;
    isLoading = false;
    document.getElementById("loading").style.display = "none";
}

window.addEventListener("scroll", () => {
    const nearBottom =
        window.innerHeight + window.scrollY >= document.body.offsetHeight - 120;

    if (nearBottom) {
        loadMessages();
    }
});

function formatTime(iso) {
    return new Date(iso).toLocaleString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
        day: "2-digit",
        month: "2-digit"
    });
}

function renderMessageMedias(medias) {
    if (!medias || medias.length === 0) return "";

    return `
        <div class="media-list">
            ${medias.map(url => `
                <img
                    src="${url}"
                    class="message-image"
                    onclick="window.open('${url}', '_blank')"
                 alt=""/>
            `).join("")}
        </div>
    `;
}

const USER_FOR_ADD_QUERY = `
query ($chatRoomId: ID!, $keyword: String!, $page: Int!, $size: Int!) {
  userForAddRoomChat(
    chatRoomId: $chatRoomId
    keyword: $keyword
    page: $page
    size: $size
  ) {
    data {
      userId
      isFollowing
      inRoom
      userForChatRoom {
        id
        userFullName
        avatarUrl
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

const ADD_MEMBER_MUTATION = `
mutation ($request: UserForAddChatRoomRequest!) {
  addMemberForChatRoom(request: $request) {
    success
    message
  }
}
`;

let addMemberPage = 0;
let addMemberHasNext = true;
let addMemberLoading = false;
let currentKeyword = "";

async function searchUserForAdd() {
    const keyword = document.getElementById("searchUserInput").value.trim();
    if (!keyword) return;

    isViewingMembers = false;

    currentKeyword = keyword;
    addMemberPage = 0;
    addMemberHasNext = true;
    document.getElementById("addMemberContainer").innerHTML = "";

    await loadUserForAdd(currentKeyword);
}

async function loadUserForAdd(keyword) {
    if (!addMemberHasNext || addMemberLoading) return;

    addMemberLoading = true;
    document.getElementById("addMemberLoading").style.display = "block";

    const res = await graphqlRequest(USER_FOR_ADD_QUERY, {
        chatRoomId: Number(chatRoomId),
        keyword,
        page: addMemberPage,
        size: 10
    });

    if (res.errors) {
        console.error("GraphQL Errors:", res.errors);
        addMemberLoading = false;
        document.getElementById("addMemberLoading").style.display = "none";
        return;
    }

    const response = res.data.userForAddRoomChat;
    renderUserForAdd(response.data);

    addMemberHasNext = response.pageInfo.hasNext;
    addMemberPage++;

    addMemberLoading = false;
    document.getElementById("addMemberLoading").style.display = "none";
}

function renderUserForAdd(users) {
    const container = document.getElementById("addMemberContainer");

    users.forEach(u => {
        const avatar = u.userForChatRoom.avatarUrl || "/icon/default-avatar.png";
        const div = document.createElement("div");
        div.className = "add-user-item";
        div.style.cssText = `display:flex; align-items:center; justify-content:space-between; padding:10px; border-bottom:1px solid #eee; gap:10px;`;

        div.innerHTML = `
            <div class="user-info" style="display:flex; gap:10px; align-items:center; cursor:pointer;">
                <img src="${avatar}" style="width:40px; height:40px; border-radius:50%; object-fit:cover;" alt="">
                <div>
                    <div style="font-weight:600">${u.userForChatRoom.userFullName}</div>
                    <div class="follow-status" style="font-size:12px; color:#666">
                        ${u.isFollowing ? "Đang theo dõi" : "Chưa theo dõi"}
                    </div>
                </div>
            </div>

            <div class="action-box" style="display:flex; gap:6px; flex-shrink:0; align-items:center;">
                ${!u.isFollowing ? `
                    <button class="follow-btn" style="padding:6px 10px; border:1px solid #007bff; background:#fff; color:#007bff; border-radius:6px; cursor:pointer">
                        Theo dõi
                    </button>` : ''
        }

                <div class="add-action-wrapper">
                    ${u.inRoom ? `
                        <span style="color:#28a745; font-size:13px; font-weight:500;">
                           <i class="fa fa-check"></i> Đã tham gia
                        </span>` : `
                        <button class="add-btn" style="padding:6px 10px; border:none; border-radius:6px; background:#007bff; color:#fff; cursor:pointer">
                            Thêm
                        </button>`
        }
                </div>
            </div>
        `;

        // Logic xử lý Event (như cũ)
        div.querySelector(".user-info").addEventListener("click", () => {
            window.location.href = `user.html?id=${u.userId}`;
        });

        const followBtn = div.querySelector(".follow-btn");
        if (followBtn) {
            followBtn.addEventListener("click", e => {
                e.stopPropagation();
                followUser(u.userId, followBtn, div.querySelector(".follow-status"));
            });
        }

        const addBtn = div.querySelector(".add-btn");
        if (addBtn) {
            addBtn.addEventListener("click", e => {
                e.stopPropagation();
                addMember(u.userId, addBtn, div.querySelector(".add-action-wrapper"));
            });
        }

        container.appendChild(div);
    });
}

const addMemberPanel = document.querySelector(".add-member-panel");

addMemberPanel.addEventListener("scroll", () => {
    const nearBottom =
        addMemberPanel.scrollTop +
        addMemberPanel.clientHeight >=
        addMemberPanel.scrollHeight - 80;

    if (!nearBottom) return;

    if (isViewingMembers) {
        loadMembers();
    } else {
        loadUserForAdd(currentKeyword);
    }
});

async function addMember(memberId, btn) {
    btn.disabled = true;
    btn.innerText = "Đang thêm...";

    const res = await graphqlRequest(ADD_MEMBER_MUTATION, {
        request: {
            memberId,
            chatRoomId: Number(chatRoomId)
        }
    });

    if (res.errors || !res.data.addMemberForChatRoom.success) {
        alert(res.errors?.[0]?.message || "Thêm thất bại");
        btn.disabled = false;
        btn.innerText = "Thêm";
        return;
    }

    btn.innerText = "Đã thêm";
    btn.style.background = "#28a745";
}

document.getElementById("searchUserBtn")
    .addEventListener("click", searchUserForAdd);

document.getElementById("searchUserInput")
    .addEventListener("keypress", e => {
        if (e.key === "Enter") searchUserForAdd();
    });

async function followUser(userId, btn, statusTextEl) {
    btn.disabled = true;
    btn.innerText = "Đang theo dõi...";

    try {
        const res = await fetch(`${URL_BASE}/follow?userId=${userId}`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
            throw new Error(result.message || "Follow thất bại");
        }

        btn.remove();
        statusTextEl.innerText = "Đang theo dõi";
    } catch (e) {
        alert(e.message);
        btn.disabled = false;
        btn.innerText = "Theo dõi";
    }
}

const MEMBERS_QUERY = `
query ($chatRoomId: ID!, $page: Int!, $size: Int!) {
  members(chatRoomId: $chatRoomId, page: $page, size: $size) {
    data {
      userId
      createdAt
      user {
        userFullName
        avatarUrl
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

let memberPage = 0;
let memberHasNext = true;
let memberLoading = false;
let isViewingMembers = false;

async function loadMembers() {
    if (!memberHasNext || memberLoading) return;

    memberLoading = true;
    document.getElementById("addMemberLoading").style.display = "block";

    const res = await graphqlRequest(MEMBERS_QUERY, {
        chatRoomId: Number(chatRoomId),
        page: memberPage,
        size: 10
    });

    if (res.errors) {
        console.error(res.errors);
        memberLoading = false;
        document.getElementById("addMemberLoading").style.display = "none";
        return;
    }

    const response = res.data.members;
    renderMembers(response.data);

    memberHasNext = response.pageInfo.hasNext;
    memberPage++;

    memberLoading = false;
    document.getElementById("addMemberLoading").style.display = "none";
}

function renderMembers(members) {
    const container = document.getElementById("addMemberContainer");

    members.forEach(m => {
        const avatar = m.user?.avatarUrl || "/icon/default-avatar.png";
        const fullName = m.user?.userFullName || "Unknown";

        const div = document.createElement("div");
        div.className = "member-item";
        div.style.cssText = `
            display:flex;
            align-items:center;
            gap:10px;
            padding:10px;
            border-bottom:1px solid #eee;
            cursor:pointer;
        `;

        div.innerHTML = `
            <img src="${avatar}"
                 style="width:40px;height:40px;border-radius:50%;object-fit:cover"  alt=""/>
            <div>
                <div style="font-weight:600">${fullName}</div>
                <div style="font-size:12px;color:#666">
                    Tham gia: ${formatTime(m.createdAt)}
                </div>
            </div>
        `;

        div.onclick = () => {
            window.location.href = `user.html?id=${m.userId}`;
        };

        container.appendChild(div);
    });
}

document.getElementById("toggleMembersBtn")
    .addEventListener("click", () => {

        isViewingMembers = true;

        // reset state
        memberPage = 0;
        memberHasNext = true;

        // clear list cũ
        document.getElementById("addMemberContainer").innerHTML = "";

        loadMembers();
    });

const sendBtn = document.getElementById("sendBtn");
const messageInput = document.getElementById("messageInput");
const mediaInput = document.getElementById("mediaInput");
const previewContainer = document.getElementById("previewContainer");
let selectedFiles = [];

mediaInput.addEventListener("change", () => {
    previewContainer.innerHTML = "";
    selectedFiles = [];

    [...mediaInput.files].forEach(file => {
        selectedFiles.push(file);

        const img = document.createElement("img");
        img.src = URL.createObjectURL(file);
        previewContainer.appendChild(img);
    });
});

sendBtn.addEventListener("click", sendMessage);
messageInput.addEventListener("keypress", e => {
    if (e.key === "Enter") sendMessage();
});

async function sendMessage() {
    const text = messageInput.value.trim();

    if (!text && selectedFiles.length === 0) return;

    const formData = new FormData();
    formData.append("chatRoomId", chatRoomId);
    formData.append("message", text);

    selectedFiles.forEach(f => formData.append("medias", f));

    sendBtn.disabled = true;

    try {
        const res = await fetch(`${URL_BASE}/chat/send-in-zoom`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            },
            body: formData
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
            throw new Error(result.message || "Gửi thất bại");
        }

        // clear input
        messageInput.value = "";
        mediaInput.value = "";
        previewContainer.innerHTML = "";
        selectedFiles = [];


        // reload message mới
        prependMessage(result.data);

    } catch (e) {
        alert(e.message);
    } finally {
        sendBtn.disabled = false;
    }
}
window.sendMessage=sendMessage

function prependMessage(msg) {
    const container = document.getElementById("messagesContainer");

    const div = document.createElement("div");
    div.className = "message mine";

    div.innerHTML = `
        <div class="content">
            <div class="bubble">
                ${msg.text ? `<div class="message-text">${msg.text}</div>` : ""}
                ${renderMessageMedias(msg.media)}
            </div>
            <div class="time">${formatTime(msg.createdAt)}</div>
        </div>
    `;

    container.appendChild(div);
    window.scrollTo(0, document.body.scrollHeight);
}

function goBack() {
    window.location.href = "chat-room.html";
}

loadMessages();
