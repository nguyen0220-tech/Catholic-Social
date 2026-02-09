const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const GRAPHQL_URL = `${URL_BASE}/graphql`;

// Các phần tử DOM chính
const msgContainer = document.getElementById("messagesContainer");
const loadingEl = document.getElementById("loading");
const sidebarContent = document.querySelector(".sidebar-content");
const messageInput = document.getElementById("messageInput");
const mediaInput = document.getElementById("mediaInput");
const sendBtn = document.getElementById("sendBtn");
const previewContainer = document.getElementById("previewContainer");

const urlParams = new URLSearchParams(window.location.search);
const chatRoomId = urlParams.get("chatRoomId");

if (!chatRoomId) {
    alert("Thiếu chatRoomId");
}

// Giải mã Token lấy ID cá nhân
function getUserIdFromToken(token) {
    if (!token) return null;
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload).id;
    } catch (e) {
        console.error("Lỗi giải mã Token:", e);
        return null;
    }
}

const currentUserId = getUserIdFromToken(accessToken);

// --- QUERIES & MUTATIONS ---
const MESSAGES_QUERY = `
query ($chatRoomId: Int!, $page: Int!, $size: Int!) {
  messages(chatRoomId: $chatRoomId, page: $page, size: $size) {
    data { id text createdAt messageMedias user { id userFullName avatarUrl } isMine }
    pageInfo { page size hasNext }
  }
}`;

const USER_FOR_ADD_QUERY = `
query ($chatRoomId: ID!, $keyword: String!, $page: Int!, $size: Int!) {
  userForAddRoomChat(chatRoomId: $chatRoomId, keyword: $keyword, page: $page, size: $size) {
    data { userId isFollowing inRoom userForChatRoom { id userFullName avatarUrl } }
    pageInfo { page size hasNext }
  }
}`;

const MEMBERS_QUERY = `
query ($chatRoomId: ID!, $page: Int!, $size: Int!) {
  members(chatRoomId: $chatRoomId, page: $page, size: $size) {
    data { userId createdAt user { userFullName avatarUrl } }
    pageInfo { page size hasNext }
  }
}`;

const MEDIA_QUERY = `
query ($chatRoomId: ID!, $page: Int!, $size: Int!) {
  messageMedias(chatRoomId: $chatRoomId, page: $page, size: $size) {
    data { id url user { userFullName avatarUrl } }
    pageInfo { page size hasNext }
  }
}`;

const ADD_MEMBER_MUTATION = `
mutation ($request: UserForAddChatRoomRequest!) {
  addMemberForChatRoom(request: $request) { success message }
}`;

// --- STATE MANAGEMENT ---
let currentPage = 0;
let hasNext = true;
let isLoading = false;

let addMemberPage = 0;
let addMemberHasNext = true;
let addMemberLoading = false;
let currentKeyword = "";

let memberPage = 0;
let memberHasNext = true;
let memberLoading = false;
let isViewingMembers = false;

let mediaPage = 0;
let mediaHasNext = true;
let mediaLoading = false;

// --- UTILS ---
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

function formatTime(iso) {
    return new Date(iso).toLocaleString("vi-VN", {
        hour: "2-digit", minute: "2-digit", day: "2-digit", month: "2-digit"
    });
}

function renderMessageMedias(medias) {
    if (!medias || medias.length === 0) return "";
    return `
        <div class="media-list">
            ${medias.map(url => `<img src="${url}" class="message-image" onclick="window.open('${url}', '_blank')" alt=""/>`).join("")}
        </div>`;
}

// --- TIN NHẮN CHAT ---
async function loadMessages() {
    if (!hasNext || isLoading) return;
    isLoading = true;
    loadingEl.style.display = "block";

    const oldScrollHeight = msgContainer.scrollHeight;

    const result = await graphqlRequest(MESSAGES_QUERY, {
        chatRoomId: Number(chatRoomId),
        page: currentPage,
        size: 10
    });

    if (!result.errors) {
        const messages = result.data.messages.data;
        hasNext = result.data.messages.pageInfo.hasNext;

        messages.forEach(msg => appendMessageToUI(msg, true));

        if (currentPage === 0) {
            msgContainer.scrollTop = msgContainer.scrollHeight;
        } else {
            msgContainer.scrollTop = msgContainer.scrollHeight - oldScrollHeight;
        }
        currentPage++;
    }
    isLoading = false;
    loadingEl.style.display = "none";
}

msgContainer.addEventListener("scroll", () => {
    if (msgContainer.scrollTop < 50 && !isLoading && hasNext) {
        loadMessages();
    }
});

function appendMessageToUI(msg, isInitialLoad = false) {
    if (document.getElementById(`msg-${msg.id}`)) return;

    const div = document.createElement("div");
    const senderId = msg.user?.id || msg.senderId;
    const isMine = (Number(senderId) === Number(currentUserId));
    const avatar = msg.user?.avatarUrl || msg.avatarUrl || '/icon/default-avatar.png';
    const mediaList = msg.messageMedias || msg.media || [];

    div.className = isMine ? "message mine" : "message other";
    div.id = `msg-${msg.id}`;
    div.innerHTML = `
        ${!isMine ? `<img class="avatar" src="${avatar}" onclick="window.location.href='user.html?id=${senderId}'" alt="">` : ""}
        <div class="content">
            <div class="bubble">
                ${msg.text ? `<div class="message-text">${msg.text}</div>` : ""}
                ${renderMessageMedias(mediaList)}
            </div>
            <div class="time">${formatTime(msg.createdAt)}</div>
        </div>
    `;

    if (isInitialLoad) {
        msgContainer.prepend(div);
    } else {
        msgContainer.appendChild(div);
        msgContainer.scrollTo({ top: msgContainer.scrollHeight, behavior: 'smooth' });
    }
}

// --- THÀNH VIÊN ---
async function loadMembers() {
    if (!memberHasNext || memberLoading) return;
    memberLoading = true;
    document.getElementById("addMemberLoading").style.display = "block";

    const res = await graphqlRequest(MEMBERS_QUERY, {
        chatRoomId: Number(chatRoomId),
        page: memberPage,
        size: 10
    });

    if (!res.errors) {
        const response = res.data.members;
        renderMembersUI(response.data);
        memberHasNext = response.pageInfo.hasNext;
        memberPage++;
    }
    memberLoading = false;
    document.getElementById("addMemberLoading").style.display = "none";
}

function renderMembersUI(members) {
    const container = document.getElementById("addMemberContainer");
    members.forEach(m => {
        const avatar = m.user?.avatarUrl || "/icon/default-avatar.png";
        const div = document.createElement("div");
        div.style.cssText = `display:flex; align-items:center; gap:10px; padding:10px; border-bottom:1px solid #eee; cursor:pointer;`;
        div.innerHTML = `
            <img src="${avatar}" style="width:35px;height:35px;border-radius:50%;object-fit:cover" alt="">
            <div>
                <div style="font-weight:600; font-size:14px;">${m.user?.userFullName || "User"}</div>
                <div style="font-size:11px;color:#888">${formatTime(m.createdAt)}</div>
            </div>`;
        div.onclick = () => window.location.href = `user.html?id=${m.userId}`;
        container.appendChild(div);
    });
}

// --- TÌM KIẾM NGƯỜI DÙNG ---
async function searchUserForAdd() {
    const keyword = document.getElementById("searchUserInput").value.trim();
    if (!keyword) return;
    isViewingMembers = false;
    currentKeyword = keyword;
    addMemberPage = 0;
    addMemberHasNext = true;
    document.getElementById("addMemberContainer").innerHTML = "";
    await loadUserForAdd(keyword);
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

    if (!res.errors) {
        const response = res.data.userForAddRoomChat;
        renderUserForAddUI(response.data);
        addMemberHasNext = response.pageInfo.hasNext;
        addMemberPage++;
    }
    addMemberLoading = false;
    document.getElementById("addMemberLoading").style.display = "none";
}

function renderUserForAddUI(users) {
    const container = document.getElementById("addMemberContainer");
    users.forEach(u => {
        const avatar = u.userForChatRoom.avatarUrl || "/icon/default-avatar.png";
        const div = document.createElement("div");
        div.className = "add-user-item";
        div.style.cssText = `display:flex; align-items:center; justify-content:space-between; padding:10px; border-bottom:1px solid #eee; gap:10px;`;

        div.innerHTML = `
            <div style="display:flex; gap:10px; align-items:center; cursor:pointer;" onclick="window.location.href='user.html?id=${u.userId}'">
                <img src="${avatar}" style="width:35px; height:35px; border-radius:50%; object-fit:cover;" alt="">
                <div>
                    <div style="font-weight:600; font-size:14px;">${u.userForChatRoom.userFullName}</div>
                    <div class="follow-status" style="font-size:11px; color:#999">
                        ${u.isFollowing ? "Đang theo dõi" : "Chưa theo dõi"}
                    </div>
                </div>
            </div>
            <div class="action-group" style="display:flex; gap:8px; align-items:center;">
                ${!u.isFollowing ? `
                    <button class="follow-btn" style="padding:4px 8px; border:1px solid #007bff; border-radius:4px; background:white; color:#007bff; cursor:pointer; font-size:12px;">Theo dõi</button>
                ` : ''}
                
                <div class="add-action-wrapper">
                    ${u.inRoom ? `<small style="color:green">Đã vào</small>` :
            `<button class="add-btn" style="padding:4px 10px; border:none; border-radius:4px; background:#007bff; color:#fff; cursor:pointer; font-size:12px;">Thêm</button>`}
                </div>
            </div>`;

        // Xử lý sự kiện cho nút Theo dõi
        const followBtn = div.querySelector(".follow-btn");
        if (followBtn) {
            followBtn.onclick = (e) => {
                e.stopPropagation();
                followUser(u.userId, followBtn, div.querySelector(".follow-status"));
            };
        }

        // Xử lý sự kiện cho nút Thêm vào phòng
        const addBtn = div.querySelector(".add-btn");
        if(addBtn) addBtn.onclick = (e) => {
            e.stopPropagation();
            addMember(u.userId, addBtn);
        }

        container.appendChild(div);
    });
}

async function followUser(userId, btn, statusTextEl) {
    btn.disabled = true;
    const originalText = btn.innerText;
    btn.innerText = "...";

    try {
        const res = await fetch(`${URL_BASE}/follow?userId=${userId}`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const result = await res.json();

        if (res.ok && result.success) {
            btn.remove(); // Theo dõi xong thì xóa nút
            if (statusTextEl) statusTextEl.innerText = "Đang theo dõi";
        } else {
            alert(result.message || "Theo dõi thất bại");
            btn.disabled = false;
            btn.innerText = originalText;
        }
    } catch (e) {
        console.error("Lỗi follow:", e);
        alert("Lỗi kết nối server");
        btn.disabled = false;
        btn.innerText = originalText;
    }
}

async function addMember(memberId, btn) {
    btn.disabled = true;
    btn.innerText = "...";
    const res = await graphqlRequest(ADD_MEMBER_MUTATION, {
        request: { memberId, chatRoomId: Number(chatRoomId) }
    });
    if (res.data?.addMemberForChatRoom.success) {
        btn.parentElement.innerHTML = `<small style="color:green">Đã thêm</small>`;
    } else {
        alert("Lỗi: " + res.data.addMemberForChatRoom.message);
        btn.disabled = false;
        btn.innerText = "Thêm";
    }
}

// --- FILE PHƯƠNG TIỆN ---
async function loadChatMedia() {
    if (!mediaHasNext || mediaLoading) return;
    mediaLoading = true;
    document.getElementById("mediaLoading").style.display = "block";

    const res = await graphqlRequest(MEDIA_QUERY, {
        chatRoomId: chatRoomId, page: mediaPage, size: 12
    });

    if (!res.errors) {
        const response = res.data.messageMedias;
        const container = document.getElementById("mediaContainer");
        response.data.forEach(item => {
            const wrapper = document.createElement("div");
            wrapper.className = "media-item-wrapper";
            const isVideo = item.url.match(/\.(mp4|webm|ogg)$/i);
            wrapper.innerHTML = isVideo ? `<video src="${item.url}"></video>` : `<img src="${item.url}" loading="lazy" alt="">`;
            wrapper.onclick = () => window.open(item.url, '_blank');
            container.appendChild(wrapper);
        });
        mediaHasNext = response.pageInfo.hasNext;
        mediaPage++;
    }
    mediaLoading = false;
    document.getElementById("mediaLoading").style.display = "none";
}

// --- SIDEBAR SCROLL EVENT (Infinite Scroll cho Sidebar) ---
sidebarContent.addEventListener("scroll", () => {
    const nearBottom = sidebarContent.scrollTop + sidebarContent.clientHeight >= sidebarContent.scrollHeight - 50;
    if (!nearBottom) return;

    const currentType = document.getElementById('rightSidebar').dataset.current;
    if (currentType === 'members') {
        if (isViewingMembers) loadMembers();
        else loadUserForAdd(currentKeyword);
    } else if (currentType === 'media') {
        loadChatMedia();
    }
});

// --- GỬI TIN NHẮN ---
let selectedFiles = [];
mediaInput.addEventListener("change", () => {
    previewContainer.innerHTML = "";
    selectedFiles = [...mediaInput.files];
    selectedFiles.forEach(file => {
        const img = document.createElement("img");
        img.src = URL.createObjectURL(file);
        previewContainer.appendChild(img);
    });
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
            headers: { "Authorization": `Bearer ${accessToken}` },
            body: formData
        });
        const result = await res.json();
        if (result.success) {
            messageInput.value = "";
            mediaInput.value = "";
            previewContainer.innerHTML = "";
            selectedFiles = [];
        }
    } catch (e) { alert("Lỗi gửi tin"); }
    finally { sendBtn.disabled = false; }
}

// --- WEB SOCKET ---
function connectWebSocket() {
    const socket = new SockJS(`${URL_BASE}/ws`);
    const stompClient = Stomp.over(socket);
    stompClient.connect({ 'Authorization': `Bearer ${accessToken}` }, () => {
        stompClient.subscribe(`/queue/message${chatRoomId}`, (output) => {
            appendMessageToUI(JSON.parse(output.body));
        });
    });
}

// --- INITIALIZE & EVENT LISTENERS ---
document.getElementById("toggleMembersBtn").addEventListener("click", () => {
    isViewingMembers = true;
    memberPage = 0;
    memberHasNext = true;
    document.getElementById("addMemberContainer").innerHTML = "";
    loadMembers();
});

document.getElementById("viewMediaBtn").addEventListener("click", () => {
    if (document.getElementById("mediaContainer").innerHTML === "") {
        mediaPage = 0;
        mediaHasNext = true;
        loadChatMedia();
    }
});

document.getElementById("searchUserBtn").onclick = searchUserForAdd;
document.getElementById("searchUserInput").onkeypress = (e) => { if (e.key === "Enter") searchUserForAdd(); };
sendBtn.onclick = sendMessage;
messageInput.onkeypress = (e) => { if (e.key === "Enter") sendMessage(); };

loadMessages();
connectWebSocket();