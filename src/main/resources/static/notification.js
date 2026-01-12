const URL_BASE = window.location.origin;
let notificationData = [];
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

const QUERY_NOTIFICATIONS = `
query($page:Int!, $size:Int!) {
  notifications(page: $page, size: $size) {
    data {
      id
      isRead
      type
      createdAt
      actor {
        id
        userFullName
        avatarUrl
      }
      target {
        __typename

        ... on MomentGQLDTO {
          id
          content
          images
        }

        ... on NotificationFollowerDTO {
          followerId
          reverseFollowed
        }

        ... on NotificationCommentDTO {
          momentId
          momentComment
          moment {
            id
            content
            images
          }
        }
      }
    }
  }
}
`;

const MUTATION_MASK_AS_READ = `
mutation($notificationId: ID!) {
  maskAsRead(notificationId: $notificationId) {
    success
  }
}
`;

const MUTATION_DELETE_NOTIFICATION = `
mutation($notificationId: ID!) {
  deleteNotification(notificationId: $notificationId) {
    success
    message
  }
}
`;


function renderTarget(target) {
    if (!target) return "";

    switch (target.__typename) {

        case "MomentGQLDTO":
            return `
                <div class="moment">
                    <span>đã thích bài viết của bạn</span>
                    <div class="moment-content">${target.content}</div>
                    ${renderImages(target.images)}
                </div>
            `;

        case "NotificationFollowerDTO":
            return `
                <div class="moment">
                    <span>đã theo dõi bạn</span><br/>
                    ${
                !target.reverseFollowed
                    ? `
                    <button
                      style="background-color:#EE82EE;color:#00fb00"
                      onclick="event.stopPropagation(); followBack(${target.followerId})">
                      Theo dõi lại
                    </button>
                      `
                    : `<span class="followed">Đang theo dõi</span>`
            }
                </div>
            `;

        case "NotificationCommentDTO":
            return `
                <div class="moment">
                    <span>đã bình luận:</span>
                    <div class="moment-content">"${target.momentComment}"</div>
                    <div class="moment-content">${target.moment.content}</div>
                    ${renderImages(target.moment.images)}
                </div>
            `;

        default:
            return "";
    }
}

function renderImages(images = []) {
    if (!images.length) return "";

    return `
        <div class="images">
            ${images.map(img => `<img src="${img}"  alt=""/>`).join("")}
        </div>
    `;
}

function renderNotificationItem(noti, index) {
    const actor = noti.actor;

    return `
        <div class="notification ${!noti.isRead ? "unread" : ""}"
             onclick="handleNotificationClick(notificationData[${index}])">

            <img class="avatar"
                 src="${actor.avatarUrl ?? "/default-avatar.png"}"
                 alt=""/>

            <div class="content">
                <div class="header-row">
                    <span class="actor-name">${actor.userFullName}</span>
                    <span class="time">${formatTimeAgo(noti.createdAt)}</span>
                </div>

                ${renderTarget(noti.target)}
            </div>

            <div class="delete-btn"
                 onclick="event.stopPropagation(); deleteNoti(${noti.id}, ${index})">
                🗑
            </div>
        </div>
    `;
}

function formatTimeAgo(isoTime) {
    if (!isoTime) return "";

    const now = new Date();
    const time = new Date(isoTime);
    const diffMs = now - time;

    const diffSeconds = Math.floor(diffMs / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffSeconds < 60) return "Vừa xong";
    if (diffMinutes < 60) return `${diffMinutes} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;

    // fallback: hiển thị ngày
    return time.toLocaleDateString("vi-VN");
}



async function loadNotifications() {
    const res = await graphqlRequest(QUERY_NOTIFICATIONS, {
        page: 0,
        size: 10
    });

    notificationData = res.data.notifications.data;
    window.notificationData = notificationData;

    const container = document.getElementById("notification-list");

    container.innerHTML = notificationData
        .map((n, i) => renderNotificationItem(n, i))
        .join("");
}

async function markAsRead(notificationId) {
    try {
        await graphqlRequest(MUTATION_MASK_AS_READ, {
            notificationId
        });
    } catch (e) {
        console.error("maskAsRead error", e);
    }
}

async function handleNotificationClick(noti) {
    if (!noti.isRead) {
        await markAsRead(noti.id);
    }

    const target = noti.target;

    switch (target.__typename) {

        case "NotificationFollowerDTO":
            window.location.href = `user.html?id=${target.followerId}`;
            break;

        case "MomentGQLDTO":
            window.location.href = `moment-detail.html?id=${target.id}`;
            break;

        case "NotificationCommentDTO":
            window.location.href = `moment-detail.html?id=${target.moment.id}`;
            break;
    }
}

window.handleNotificationClick = handleNotificationClick

async function followBack(userId) {
    try {
        const res = await fetch(`${URL_BASE}/follow?userId=${userId}`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        const json = await res.json();

        if (json.success) {
            loadNotifications();
        } else {
            alert(json.message || "Follow failed");
        }

    } catch (e) {
        console.error("followBack error", e);
    }
}
window.followBack=followBack

async function deleteNoti(notificationId, index) {
    const ok = confirm("Xóa thông báo này?");
    if (!ok) return;

    try {
        const res = await graphqlRequest(MUTATION_DELETE_NOTIFICATION, {
            notificationId
        });

        if (res.data.deleteNotification.success) {
            // xóa khỏi mảng
            notificationData.splice(index, 1);

            // render lại UI
            const container = document.getElementById("notification-list");
            container.innerHTML = notificationData
                .map((n, i) => renderNotificationItem(n, i))
                .join("");
        } else {
            alert(res.data.deleteNotification.message);
        }

    } catch (e) {
        console.error("deleteNotification error", e);
    }
}

window.deleteNoti = deleteNoti;

loadNotifications();