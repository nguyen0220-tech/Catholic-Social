const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const GRAPHQL_URL = `${URL_BASE}/graphql`;

let currentPage = 0;
const pageSize = 5;
let hasNext = true;
let isLoading = false;


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

const ROOMS_QUERY = `
query ($page: Int!, $size: Int!) {
  roomsChat(page: $page, size: $size) {
    data {
      chatRoomId
      detail {
        roomName
        description
        lastMessagePreview
        lastMessageAt
      }
      members {
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

const UPDATE_ROOM_MUTATION = `
mutation ($request: UpdateChatRoomRequest!) {
  updateChatRoom(request: $request) {
    success
    message
    data
  }
}
`;


async function loadRooms() {
    if (!hasNext || isLoading) return;

    isLoading = true;
    document.getElementById("loading").style.display = "block";

    const result = await graphqlRequest(ROOMS_QUERY, {
        page: currentPage,
        size: pageSize
    });

    if (result.errors) {
        console.error(result.errors);
        isLoading = false;
        return;
    }

    const response = result.data.roomsChat;
    const rooms = response.data;
    hasNext = response.pageInfo.hasNext;

    const container = document.getElementById("roomsContainer");

    rooms.forEach(room => {
        const roomDiv = document.createElement("div");
        roomDiv.className = "room";
        roomDiv.setAttribute("data-room-id", room.chatRoomId); // THÊM DÒNG NÀY

        roomDiv.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; cursor: pointer">
            <h3 onclick="openChatRoom('${room.chatRoomId}')">
                ${renderRoomTitle(room)}
            </h3>
            <div style="display:flex; align-items:center; gap:8px">
                <span class="last-message-time" style="font-size:12px; color:#888">
                    ${formatTime(room.detail.lastMessageAt)}
                </span>
                <button style="border: none; background-color: #ffffff" onclick='event.stopPropagation(); openEditRoom(${JSON.stringify(room)})'>✏️</button>
                <button style="border: none; background-color: #ffffff" onclick="event.stopPropagation(); leaveChatRoom('${room.chatRoomId}')">🚪</button>
            </div>
        </div>
        <div class="message-row" onclick="openChatRoom('${room.chatRoomId}')">
            ${renderMembers(room.members)}
            <div class="last-message">
                ${room.detail.lastMessagePreview ?? "<i>Chưa có tin nhắn</i>"}
            </div>
        </div>
    `;
        container.appendChild(roomDiv);
    });

    currentPage++;
    isLoading = false;
    document.getElementById("loading").style.display = "none";
}

window.addEventListener("scroll", () => {
    const nearBottom =
        window.innerHeight + window.scrollY >= document.body.offsetHeight - 150;

    if (nearBottom) {
        loadRooms();
    }
});

function renderRoomTitle(room) {
    const title = getRoomTitle(room);
    const desc = room.detail.description;

    if (desc && desc.trim() !== "") {
        return `
            <span>${title}</span>
            <span style="font-size:12px; color:#888; margin-left:6px">
                • ${desc}
            </span>
        `;
    }

    return `<span>${title}</span>`;
}


function getRoomTitle(room) {
    const roomName = room.detail.roomName;

    if (roomName && roomName.trim() !== "") {
        return roomName;
    }

    if (!room.members || room.members.length === 0) {
        return "Phòng chat";
    }

    const maxNames = 2;
    const names = room.members.map(m => m.userFullName);

    const visible = names.slice(0, maxNames);
    const remaining = names.length - maxNames;

    return remaining > 0
        ? `${visible.join(", ")}, +${remaining}`
        : visible.join(", ");
}

function renderMembers(members) {
    if (!members || members.length === 0) return "";

    const maxVisible = 4;
    const visibleMembers = members.slice(0, maxVisible);
    const remaining = members.length - maxVisible;

    const sizeClass =
        members.length === 1 ? "large" :
            members.length <= 3 ? "medium" :
                "small";

    return `
        <div class="avatar-stack">
            ${visibleMembers.map(m => `
                <img
                    src="${m.avatarUrl}"
                    class="avatar ${sizeClass}"
                    alt=""
                >
            `).join("")}

            ${remaining > 0
        ? `<div class="more-count">+${remaining}</div>`
        : ""}
        </div>
    `;
}

function formatTime(isoString) {
    if (!isoString) return "";

    const date = new Date(isoString);
    return date.toLocaleString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
        day: "2-digit",
        month: "2-digit"
    });
}

//update room
let editingRoomId = null;

function openEditRoom(room) {
    editingRoomId = room.chatRoomId;

    document.getElementById("roomNameInput").value =
        room.detail.roomName ?? "";

    document.getElementById("roomDescInput").value =
        room.detail.description ?? "";

    document.getElementById("editRoomModal").style.display = "flex";
}

window.openEditRoom = openEditRoom

function closeEditRoom() {
    editingRoomId = null;
    document.getElementById("editRoomModal").style.display = "none";
}

window.closeEditRoom = closeEditRoom

async function submitUpdateRoom() {
    const name = document.getElementById("roomNameInput").value.trim();
    const desc = document.getElementById("roomDescInput").value.trim();

    const result = await graphqlRequest(UPDATE_ROOM_MUTATION, {
        request: {
            chatRoomId: editingRoomId,
            chatRoomName: name || null,
            chatRoomDescription: desc || null
        }
    });

    if (!result.data.updateChatRoom.success) {
        alert(result.data.updateChatRoom.message);
        return;
    }

    closeEditRoom();
}
window.submitUpdateRoom=submitUpdateRoom

function openChatRoom(chatRoomId) {
    window.location.href = `message.html?chatRoomId=${chatRoomId}`;
}
window.openChatRoom=openChatRoom

const LEAVE_ROOM_MUTATION = `
mutation ($chatRoomId: ID!) {
  leaveChatRoom(chatRoomId: $chatRoomId) {
    success
    message
    data
  }
}
`;

async function leaveChatRoom(chatRoomId) {
    const ok = confirm("Bạn có chắc muốn thoát khỏi phòng chat này không?");
    if (!ok) return;

    const res = await graphqlRequest(LEAVE_ROOM_MUTATION, {
        chatRoomId
    });

    if (res.errors || !res.data.leaveChatRoom.success) {
        alert(res.errors?.[0]?.message || res.data.leaveChatRoom.message);
        return;
    }

    const roomsContainer = document.getElementById("roomsContainer");
    const roomDivs = [...roomsContainer.children];

    const target = roomDivs.find(div =>
        div.innerHTML.includes(chatRoomId)
    );

    if (target) target.remove();

    currentPage = 0;
    hasNext = true;
    roomsContainer.innerHTML = "";
    loadRooms();
}
window.leaveChatRoom = leaveChatRoom;

const USERS_RECENT_MESSAGE_QUERY = `
query {
  usersRecentMessage {
    id
    user {
      id
      userFullName
      avatarUrl
    }
  }
}
`;

async function loadRecentUsersForGroup() {
    const container = document.getElementById("recentUsersContainer");
    container.innerHTML = "⏳ Đang tải...";

    const res = await graphqlRequest(USERS_RECENT_MESSAGE_QUERY);
    if (res.errors) {
        container.innerHTML = "❌ Không tải được danh sách";
        return;
    }

    // Map lại data để giống cấu trúc của query Search cho dễ render
    const formattedData = res.data.usersRecentMessage.map(item => ({
        userId: item.user.id,
        user: item.user
    }));

    renderUserList(formattedData);
}

const USERS_FOR_CREATE_ROOM_QUERY = `
query ($keyword: String!, $page: Int!, $size: Int!) {
  usersForCreateRoomChat(keyword: $keyword, page: $page, size: $size) {
    data {
      userId
      user {
        userFullName
        avatarUrl
      }
      isFollowing
    }
    pageInfo {
      hasNext
    }
  }
}
`;

// Biến để lưu trữ trạng thái chọn user
let selectedUserIds = new Set();

// Hàm Debounce: Đợi người dùng dừng gõ 500ms mới gọi API
function debounce(func, timeout = 500) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
}

// Lắng nghe sự kiện gõ phím ở ô tìm kiếm
document.getElementById("userSearchInput").addEventListener("input", debounce((e) => {
    const keyword = e.target.value.trim();
    if (keyword.length > 0) {
        fetchUsers(keyword);
    } else {
        loadRecentUsersForGroup(); // Trở lại danh sách gần đây nếu ô search trống
    }
}));

async function fetchUsers(keyword) {
    const container = document.getElementById("recentUsersContainer");
    container.innerHTML = "🔍 Đang tìm...";

    const res = await graphqlRequest(USERS_FOR_CREATE_ROOM_QUERY, {
        keyword: keyword,
        page: 0,
        size: 20
    });

    if (res.errors) {
        container.innerHTML = "❌ Lỗi tìm kiếm";
        return;
    }

    renderUserList(res.data.usersForCreateRoomChat.data);
}

function renderUserList(users) {
    const container = document.getElementById("recentUsersContainer");

    if (!users || users.length === 0) {
        container.innerHTML = "<i style='padding:8px; display:block'>Không tìm thấy người dùng nào</i>";
        return;
    }

    container.innerHTML = users.map(u => {
        const uid = u.userId || u.user.id;
        const isChecked = selectedUserIds.has(uid) ? "checked" : "";

        // Tạo nhãn "Đang theo dõi" nếu isFollowing là true
        const followingBadge = u.isFollowing
            ? `<span style="
                font-size: 10px; 
                background: #e1f5fe; 
                color: #039be5; 
                padding: 2px 6px; 
                border-radius: 10px; 
                margin-left: 8px;
                font-weight: 600;
                border: 1px solid #b3e5fc;
              ">Đang theo dõi</span>`
            : "";

        return `
        <label style="
            display:flex; 
            align-items:center; 
            gap:10px; 
            padding:10px 8px; 
            cursor:pointer; 
            border-bottom: 1px solid #f0f0f0;
            transition: background 0.2s;
        " onmouseover="this.style.background='#f9f9f9'" onmouseout="this.style.background='transparent'">
            
            <input type="checkbox" class="group-member-checkbox" value="${uid}" 
                   ${isChecked} onchange="toggleUserSelection('${uid}')" 
                   style="width: 16px; height: 16px; cursor: pointer;">
            
            <img src="${u.user.avatarUrl || '/icon/default-avatar.png'}" 
                 style="width:38px; height:38px; border-radius:50%; object-fit: cover; border: 1px solid #eee;" alt="">
            
            <div style="display: flex; flex-direction: column; flex-grow: 1;">
                <div style="display: flex; align-items: center;">
                    <span style="font-weight: 500; color: #333;">${u.user.userFullName}</span>
                    ${followingBadge}
                </div>
                </div>
        </label>
        `;
    }).join("");
}

// Cập nhật Set khi check/uncheck
window.toggleUserSelection = function(userId) {
    if (selectedUserIds.has(userId)) {
        selectedUserIds.delete(userId);
    } else {
        selectedUserIds.add(userId);
    }
}

const CREATE_GROUP_CHAT_MUTATION = `
mutation ($request: GroupChatRequest!) {
  createGroupChat(request: $request) {
    success
    message
    data {
      chatRoomId
      roomName
      roomDescription
      lastMessagePreview
      lastMessageAt
    }
  }
}
`;

async function openCreateGroup() {
    selectedUserIds.clear();
    document.getElementById("userSearchInput").value = "";
    document.getElementById("groupNameInput").value = "";
    document.getElementById("createGroupModal").style.display = "flex";
    loadRecentUsersForGroup();
}
window.openCreateGroup = openCreateGroup;


function closeCreateGroup() {
    document.getElementById("createGroupModal").style.display = "none";
}
window.closeCreateGroup = closeCreateGroup;

async function submitCreateGroup() {
    const name = document.getElementById("groupNameInput").value.trim();
    const desc = document.getElementById("groupDescInput").value.trim();
    const memberIds = Array.from(selectedUserIds); // Lấy từ Set

    if (memberIds.length === 0) {
        alert("Hãy chọn ít nhất 1 thành viên");
        return;
    }

    const res = await graphqlRequest(CREATE_GROUP_CHAT_MUTATION, {
        request: {
            memberIds,
            roomName: name || null,
            roomDescription: desc || null
        }
    });

    if (res.errors || !res.data.createGroupChat.success) {
        alert("Lỗi: " + (res.errors?.[0]?.message || res.data.createGroupChat.message));
        return;
    }

    closeCreateGroup();
    // Có thể thêm logic: chuyển hướng vào phòng vừa tạo
    if(res.data.createGroupChat.data.chatRoomId) {
        openChatRoom(res.data.createGroupChat.data.chatRoomId);
    }
}
window.submitCreateGroup=submitCreateGroup

// --- 1. LẤY USER ID TỪ TOKEN ---
function getUserIdFromToken(token) {
    if (!token) return null;
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
            '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
        return JSON.parse(jsonPayload).id;
    } catch (e) { return null; }
}
const currentUserId = getUserIdFromToken(accessToken);

// --- 2. KẾT NỐI WEBSOCKET ---
let stompClient = null;
function connectRoomWebSocket() {
    const socket = new SockJS(`${URL_BASE}/ws`);
    stompClient = Stomp.over(socket);

    stompClient.connect({'Authorization': `Bearer ${accessToken}`}, () => {
        console.log('Connected to Room List channel');

        // Subscribe vào kênh cá nhân
        stompClient.subscribe(`/queue/rooms-${currentUserId}`, (messageOutput) => {
            const updateData = JSON.parse(messageOutput.body);
            updateRoomUI(updateData);
        });
    });
}

function updateRoomUI(update) {
    const container = document.getElementById("roomsContainer");
    const roomDiv = document.querySelector(
        `[data-room-id="${update.chatRoomId}"]`
    );

    if (roomDiv) {
        const titleEl = roomDiv.querySelector("h3");
        if (titleEl) {
            let html = `<span>${update.roomName ?? "Nhóm chat"}</span>`;

            if (update.roomDescription && update.roomDescription.trim() !== "") {
                html += `
                    <span style="font-size:12px; color:#888; margin-left:6px">
                        • ${update.roomDescription}
                    </span>
                `;
            }

            titleEl.innerHTML = html;
        }

        const lastMsgEl = roomDiv.querySelector(".last-message");
        if (lastMsgEl && update.lastMessagePreview !== undefined) {
            lastMsgEl.innerHTML =
                update.lastMessagePreview ?? "<i>Chưa có tin nhắn</i>";
        }

        const timeEl = roomDiv.querySelector(".last-message-time");
        if (timeEl && update.lastMessageAt) {
            timeEl.innerText = formatTime(update.lastMessageAt);
        }

        container.prepend(roomDiv);
    }
    else {
        const div = document.createElement("div");
        div.className = "room";
        div.setAttribute("data-room-id", update.chatRoomId);

        div.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center">
                <h3 onclick="openChatRoom('${update.chatRoomId}')">
                    <span>${update.roomName ?? "Nhóm chat"}</span>
                    ${
            update.roomDescription
                ? `<span style="font-size:12px;color:#888;margin-left:6px">
                                • ${update.roomDescription}
                               </span>`
                : ""
        }
                </h3>
                <span class="last-message-time" style="font-size:12px;color:#888">
                    ${formatTime(update.lastMessageAt)}
                </span>
            </div>
            <div class="message-row" onclick="openChatRoom('${update.chatRoomId}')">
                <div class="last-message">
                    ${update.lastMessagePreview ?? "<i>Chưa có tin nhắn</i>"}
                </div>
            </div>
        `;

        container.prepend(div);
    }
}

connectRoomWebSocket();
loadRooms();
