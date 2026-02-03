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

window.submitUpdateRoom = submitUpdateRoom

function openChatRoom(chatRoomId) {
    window.location.href = `message.html?chatRoomId=${chatRoomId}`;
}

window.openChatRoom = openChatRoom

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
    hasRoom {
      chatRoomId
    }
  }
}
`;

async function loadRecentUsersForGroup() {
    const container = document.getElementById("recentUsersContainer");
    container.style.opacity = "0.5";

    const res = await graphqlRequest(USERS_RECENT_MESSAGE_QUERY);
    container.style.opacity = "1";

    if (res.errors) {
        container.innerHTML = "❌ Không tải được danh sách";
        return;
    }

    const formattedData = res.data.usersRecentMessage.map(item => ({
        userId: item.user.id,
        user: item.user,
        isFollowing: item.isFollowing // Đảm bảo map đủ field
    }));

    renderUserList(formattedData, false);
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
      hasRoom {
      chatRoomId
    }
    }
    pageInfo {
      hasNext
    }
  }
}
`;

// --- LOGIC SOẠN TIN NHẮN 1-1 ---
let privatePage = 0;
let privateHasNext = true;
let privateIsLoading = false;
let currentPrivateKeyword = "";

async function openPrivateMessage() {
    document.getElementById("privateSearchInput").value = "";
    document.getElementById("privateMessageModal").style.display = "flex";
    loadUsersForPrivate();
}

window.openPrivateMessage = openPrivateMessage;

function closePrivateMessage() {
    document.getElementById("privateMessageModal").style.display = "none";
}

window.closePrivateMessage = closePrivateMessage;

// Lắng nghe ô search
document.getElementById("privateSearchInput").addEventListener("input", debounce((e) => {
    const keyword = e.target.value.trim();
    loadUsersForPrivate(keyword);
}));

async function loadUsersForPrivate(isLoadMore = false) {
    if (privateIsLoading || (isLoadMore && !privateHasNext)) return;

    privateIsLoading = true;
    const container = document.getElementById("privateUsersContainer");

    // Lấy keyword từ biến toàn cục đã được cập nhật ở trình lắng nghe sự kiện
    const keyword = currentPrivateKeyword;

    if (!isLoadMore) {
        privatePage = 0;
        privateHasNext = true;
        container.innerHTML = "⏳ Đang tải...";
    } else {
        const loader = document.createElement("div");
        loader.id = "private-mini-loading";
        loader.innerHTML = "⏳ Đang tải thêm...";
        loader.style.textAlign = "center";
        loader.style.padding = "10px";
        container.appendChild(loader);
    }

    let users = [];

    if (keyword) {
        // TRƯỜNG HỢP CÓ TÌM KIẾM
        const res = await graphqlRequest(USERS_FOR_CREATE_ROOM_QUERY, {
            keyword: keyword,
            page: privatePage,
            size: 15
        });

        if (!res.errors) {
            const result = res.data.usersForCreateRoomChat;
            users = result.data.map(item => ({
                userId: item.userId,
                userFullName: item.user.userFullName,
                avatarUrl: item.user.avatarUrl,
                isFollowing: item.isFollowing,
                chatRoomId: item.hasRoom?.chatRoomId
            }));
            privateHasNext = result.pageInfo.hasNext;
        }
    } else {
        // TRƯỜNG HỢP TRỐNG (HIỆN RECENT)
        const res = await graphqlRequest(USERS_RECENT_MESSAGE_QUERY);
        if (!res.errors) {
            users = res.data.usersRecentMessage.map(item => ({
                userId: item.user.id,
                userFullName: item.user.userFullName,
                avatarUrl: item.user.avatarUrl,
                isFollowing: item.isFollowing,
                chatRoomId: item.hasRoom?.chatRoomId
            }));
            privateHasNext = false; // Thường recent không phân trang
        }
    }

    const miniLoading = document.getElementById("private-mini-loading");
    if (miniLoading) miniLoading.remove();

    renderPrivateUserList(users, isLoadMore);

    privatePage++;
    privateIsLoading = false;
}

function renderPrivateUserList(users, isLoadMore) {
    const container = document.getElementById("privateUsersContainer");

    if (!isLoadMore && (!users || users.length === 0)) {
        container.innerHTML = "<i style='padding:20px; display:block; text-align:center'>Không tìm thấy người dùng</i>";
        return;
    }

    const html = users.map(u => {
        const followingBadge = u.isFollowing ? `<span class="badge-following">Đang theo dõi</span>` : "";
        return `
        <div onclick="handleSelectUserPrivate('${u.userId}', '${u.chatRoomId || ''}')" class="user-private-item">
            <img src="${u.avatarUrl || '/icon/default-avatar.png'}" class="user-avatar-small" alt="">
            <div class="user-info-col">
                <span class="user-name-text">${u.userFullName}</span>
                ${followingBadge}
            </div>
        </div>
        `;
    }).join("");

    if (isLoadMore) {
        container.insertAdjacentHTML('beforeend', html);
    } else {
        container.innerHTML = html;
    }
}

// Khởi tạo các sự kiện khi trang load
document.addEventListener("DOMContentLoaded", () => {
    // 1. --- LOGIC CHO NHẮN TIN 1-1 (PRIVATE) ---
    const privateContainer = document.getElementById("privateUsersContainer");
    const privateSearchInput = document.getElementById("privateSearchInput");

    // Lắng nghe cuộn chuột để load thêm
    privateContainer.addEventListener("scroll", () => {
        const isBottom = privateContainer.scrollHeight - privateContainer.scrollTop <= privateContainer.clientHeight + 20;
        // Chỉ load thêm khi có từ khóa tìm kiếm và còn trang tiếp theo
        if (isBottom && privateHasNext && !privateIsLoading && currentPrivateKeyword) {
            loadUsersForPrivate(true); // true = load more
        }
    });

    // Lắng nghe ô nhập liệu tìm kiếm 1-1
    if (privateSearchInput) {
        privateSearchInput.addEventListener("input", debounce((e) => {
            currentPrivateKeyword = e.target.value.trim(); // QUAN TRỌNG: Cập nhật biến global
            loadUsersForPrivate(false); // false = tải mới từ trang 0
        }));
    }


    // 2. --- LOGIC CHO TẠO NHÓM (GROUP) ---
    const groupContainer = document.getElementById("recentUsersContainer");
    const groupSearchInput = document.getElementById("userSearchInput");

    // Lắng nghe cuộn chuột để load thêm
    groupContainer.addEventListener("scroll", () => {
        const isBottom = groupContainer.scrollHeight - groupContainer.scrollTop <= groupContainer.clientHeight + 20;
        if (isBottom && groupHasNext && !groupIsLoading && currentGroupKeyword) {
            loadUsersForGroup(true); // true = load more
        }
    });

    // Lắng nghe ô nhập liệu tìm kiếm Nhóm
    if (groupSearchInput) {
        groupSearchInput.addEventListener("input", debounce((e) => {
            currentGroupKeyword = e.target.value.trim(); // QUAN TRỌNG: Cập nhật biến global
            if (currentGroupKeyword.length > 0) {
                loadUsersForGroup(false); // tải mới kết quả tìm kiếm
            } else {
                loadRecentUsersForGroup(); // Trở lại danh sách gần đây nếu xóa trắng ô search
            }
        }));
    }
});

// Hàm xử lý khi chọn 1 user để chat 1-1
let tempRecipientId = null;
window.handleSelectUserPrivate = function (userId, chatRoomId) {
    // 1. Nếu đã có phòng rồi, đi thẳng vào phòng đó
    if (chatRoomId && chatRoomId !== "null" && chatRoomId !== "undefined" && chatRoomId !== "") {
        openChatRoom(chatRoomId);
        return;
    }

    // 2. Nếu chưa có phòng, ẩn ngay modal tìm kiếm user trước khi mở modal nhắn tin
    closePrivateMessage();

    // 3. Chuẩn bị và hiển thị modal chat nhanh (Quick Chat)
    tempRecipientId = userId;
    document.getElementById("quickChatText").value = "";
    document.getElementById("quickChatFiles").value = "";
    document.getElementById("fileCount").innerText = "";
    document.getElementById("quickChatModal").style.display = "flex";
};

// 2. Hàm đóng Modal
function closeQuickChat() {
    document.getElementById("quickChatModal").style.display = "none";
    tempRecipientId = null;
}

window.closeQuickChat = closeQuickChat

// 3. Cập nhật thông báo số lượng file đã chọn
function updateFileCount() {
    const files = document.getElementById("quickChatFiles").files;
    document.getElementById("fileCount").innerText = files.length > 0 ? `Đã chọn ${files.length} file` : "";
}

window.updateFileCount = updateFileCount

// 4. Hàm xử lý gửi tin nhắn qua REST API
async function submitQuickChat() {
    const message = document.getElementById("quickChatText").value.trim();
    const fileInput = document.getElementById("quickChatFiles");
    const btn = document.getElementById("btnSendQuickChat");

    if (!message && fileInput.files.length === 0) {
        alert("Vui lòng nhập tin nhắn!");
        return;
    }

    // Hiệu ứng loading
    btn.disabled = true;
    btn.innerText = "Đang gửi...";

    // Chuẩn bị FormData cho REST API
    const formData = new FormData();
    formData.append("recipientId", tempRecipientId);
    formData.append("message", message);
    for (let i = 0; i < fileInput.files.length; i++) {
        formData.append("medias", fileInput.files[i]);
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

        if (result.data && result.data.chatRoomId) {
            // Nhắn thành công -> chuyển hướng vào phòng chat mới
            window.location.href = `message.html?chatRoomId=${result.data.chatRoomId}`;
        } else {
            alert("Lỗi: " + (result.message || "Không thể gửi tin nhắn"));
            btn.disabled = false;
            btn.innerText = "Gửi tin nhắn";
        }
    } catch (error) {
        console.error("Gửi REST lỗi:", error);
        alert("Có lỗi xảy ra khi kết nối máy chủ");
        btn.disabled = false;
        btn.innerText = "Gửi tin nhắn";
    }
}

window.submitQuickChat = submitQuickChat

// Biến để lưu trữ trạng thái chọn user
let selectedUserIds = new Set();

// Hàm Debounce: Đợi người dùng dừng gõ 500ms mới gọi API
function debounce(func, timeout = 500) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => {
            func.apply(this, args);
        }, timeout);
    };
}

let groupPage = 0;
let groupHasNext = true;
let groupIsLoading = false;
let currentGroupKeyword = "";

async function loadUsersForGroup(isLoadMore = false) {
    if (groupIsLoading || (isLoadMore && !groupHasNext)) return;

    groupIsLoading = true;
    const container = document.getElementById("recentUsersContainer");
    const keyword = currentGroupKeyword;

    // Nếu là load more, thêm loader nhỏ ở dưới
    if (isLoadMore) {
        const loader = document.createElement("div");
        loader.id = "group-mini-loading";
        loader.innerHTML = "⏳ Đang tải thêm...";
        loader.style.textAlign = "center";
        loader.style.padding = "10px";
        container.appendChild(loader);
    }
        // Nếu là tìm kiếm mới, KHÔNG xóa innerHTML ngay để tránh nhấp nháy
    // Thay vào đó, có thể thêm một hiệu ứng mờ (opacity) cho container
    else {
        container.style.opacity = "0.5";
    }

    try {
        const res = await graphqlRequest(USERS_FOR_CREATE_ROOM_QUERY, {
            keyword: keyword,
            page: groupPage,
            size: 20
        });

        // Xóa mini loader nếu có
        const miniLoading = document.getElementById("group-mini-loading");
        if (miniLoading) miniLoading.remove();

        // Trả lại độ đậm nhạt cho container
        container.style.opacity = "1";

        if (res.errors) {
            if (!isLoadMore) container.innerHTML = "❌ Lỗi tìm kiếm";
            groupIsLoading = false;
            return;
        }

        const response = res.data.usersForCreateRoomChat;
        groupHasNext = response.pageInfo.hasNext;

        renderUserList(response.data, isLoadMore);
        groupPage++;
    } catch (error) {
        console.error(error);
        container.style.opacity = "1";
    } finally {
        groupIsLoading = false;
    }
}

function renderUserList(users, isLoadMore = false) {
    const container = document.getElementById("recentUsersContainer");

    if (!isLoadMore && (!users || users.length === 0)) {
        container.innerHTML = "<i style='padding:8px; display:block'>Không tìm thấy người dùng nào</i>";
        return;
    }

    const html = users.map(u => {
        const uid = u.userId || u.user.id;
        // Kiểm tra xem ID này đã có trong Set chưa để giữ trạng thái checkbox
        const isChecked = selectedUserIds.has(uid) ? "checked" : "";

        const followingBadge = u.isFollowing
            ? `<span class="badge-following">Đang theo dõi</span>`
            : "";

        return `
        <label class="group-user-label">
            <input type="checkbox" class="group-member-checkbox" value="${uid}" 
                   ${isChecked} onchange="toggleUserSelection('${uid}')">
            
            <img src="${u.user.avatarUrl || '/icon/default-avatar.png'}" class="user-avatar-small" alt="">
            
            <div style="display: flex; flex-direction: column; flex-grow: 1;">
                <div style="display: flex; align-items: center;">
                    <span style="font-weight: 500; color: #333;">${u.user.userFullName}</span>
                    ${followingBadge}
                </div>
            </div>
        </label>
        `;
    }).join("");

    if (isLoadMore) {
        container.insertAdjacentHTML('beforeend', html);
    } else {
        container.innerHTML = html;
    }
}

// Cập nhật Set khi check/uncheck
window.toggleUserSelection = function (userId) {
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
    if (res.data.createGroupChat.data.chatRoomId) {
        openChatRoom(res.data.createGroupChat.data.chatRoomId);
    }
}

window.submitCreateGroup = submitCreateGroup

// --- 1. LẤY USER ID TỪ TOKEN ---
function getUserIdFromToken(token) {
    if (!token) return null;
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
            '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
        return JSON.parse(jsonPayload).id;
    } catch (e) {
        return null;
    }
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
    } else {
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
