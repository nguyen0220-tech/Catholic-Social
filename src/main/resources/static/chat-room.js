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

    document.getElementById("roomsContainer").innerHTML = "";
    currentPage = 0;
    hasNext = true;
    loadRooms();
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
    const roomDiv = document.querySelector(`[data-room-id="${update.chatRoomId}"]`);

    if (roomDiv) {
        const lastMsgEl = roomDiv.querySelector(".last-message");
        const timeEl = roomDiv.querySelector(".last-message-time");

        if (lastMsgEl) lastMsgEl.innerText = update.lastMessagePreview;
        if (timeEl) timeEl.innerText = formatTime(update.lastMessageAt);

        roomDiv.style.backgroundColor = "#e7f3ff";
        setTimeout(() => roomDiv.style.backgroundColor = "transparent", 2000);

        container.prepend(roomDiv);
    } else {
        console.log("Room not in list, refreshing...");
    }
}

connectRoomWebSocket();
loadRooms();
