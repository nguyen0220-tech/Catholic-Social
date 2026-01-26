const URL_BASE = window.location.origin;
const accessToken = localStorage.getItem("accessToken");
const GRAPHQL_URL = `${URL_BASE}/graphql`;

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
            <img class="avatar" src="${msg.user.avatarUrl}" alt="">
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
                />
            `).join("")}
        </div>
    `;
}

loadMessages();
