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

const QUERY_GET_ACTIVE = `
query GetAllActive($page: Int!, $size: Int!) {
  allActive(page: $page, size: $size) {
    data {
      id
      type
      user {
        id
        userFullName
        avatarUrl
      }
      target {
        __typename

        ... on MomentUserDTO {
          id
          content
          createdAt
          imgUrls
          share
        }

        ... on CommentDTO {
          id
          comment
          commentDate
          moment {
            id
            content
            images
          }
        }

        ... on HeartDTO {
          id
          moment {
            id
            content
            images
          }
        }
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

const activityList = document.getElementById("activity-list");
const loadingEl = document.getElementById("loading");

let currentPage = 0;
const PAGE_SIZE = 5;
let hasNext = true;
let isLoading = false;

async function loadActivities() {
    if (!hasNext || isLoading) return;

    isLoading = true;
    loadingEl.classList.remove("hidden");

    let res;

    if (currentFilter === "ALL") {
        res = await graphqlRequest(QUERY_GET_ACTIVE, {
            page: currentPage,
            size: PAGE_SIZE
        });
    } else {
        res = await graphqlRequest(QUERY_GET_ACTIVE_BY_TYPE, {
            type: currentFilter,
            page: currentPage,
            size: PAGE_SIZE
        });
    }

    if (res.errors) {
        console.error("GraphQL errors:", res.errors);
        loadingEl.textContent = "Không thể tải activity";
        return;
    }

    const result =
        currentFilter === "ALL"
            ? res.data.allActive
            : res.data.activeType;

    const { data, pageInfo } = result;

    data.forEach(renderActivity);

    hasNext = pageInfo.hasNext;
    currentPage++;

    isLoading = false;
    loadingEl.classList.add("hidden");
}

/* ================= Render ================= */

function renderActivity(active) {
    const target = active.target;
    if (!target || !active.user) return;

    const momentId = extractMomentId(target);
    if (!momentId) return;

    const { userFullName, avatarUrl } = active.user;
    const userId = active.user.id;

    const div = document.createElement("div");
    div.className = "activity-item";
    div.style.cursor = "pointer";

    div.onclick = () => {
        window.location.href = `moment-detail.html?id=${momentId}`;
    };

    const actorHtml = `
        <a href="user.html?id=${userId}" 
           class="activity-actor"
           onclick="event.stopPropagation()">
            <img class="activity-avatar" src="${avatarUrl || 'icon/default-avatar.png'}"  alt=""/>
            <span class="activity-name">${userFullName}</span>
        </a>
    `;

    switch (target.__typename) {
        case "MomentUserDTO":
            div.innerHTML = `
                <div class="activity-header">
                    ${actorHtml}
                    <span class="activity-text">📤 đã đăng bài viết mới</span>
                </div>
                <div class="activity-time">${formatDate(target.createdAt)}</div>
                <div class="activity-content">
                    <p>${target.content}</p>
                    ${renderMomentPreview(target.imgUrls)}
                </div>
            `;
            break;

        case "CommentDTO":
            div.innerHTML = `
                <div class="activity-header">
                    ${actorHtml}
                    <span class="activity-text">🗯️ đã bình luận</span>
                </div>
                <div class="activity-time">${formatDate(target.commentDate)}</div>
                <div class="activity-content">
                    <p>"${target.comment}"</p>
                    <small>Bài viết: ${target.moment.content}</small>
                    ${renderMomentPreview(target.moment?.images)}
                </div>
            `;
            break;

        case "HeartDTO":
            div.innerHTML = `
                <div class="activity-header">
                    ${actorHtml}
                    <span class="activity-text">❤️ đã thích bài viết</span>
                </div>
                <div class="activity-content">
                    <p>${target.moment.content}</p>
                    ${renderMomentPreview(target.moment?.images)}
                </div>
            `;
            break;
    }

    activityList.appendChild(div);
}

function extractMomentId(target) {
    if (!target) return null;

    switch (target.__typename) {
        case "MomentUserDTO":
            return target.id;

        case "CommentDTO":
            return target.moment?.id;

        case "HeartDTO":
            return target.moment?.id;

        default:
            return null;
    }
}

const QUERY_GET_ACTIVE_BY_TYPE = `
query GetActiveByType($type: ActiveType!, $page: Int!, $size: Int!) {
  activeType(type: $type, page: $page, size: $size) {
    data {
      id
      type
      user {
        id
        userFullName
        avatarUrl
      }
      target {
        __typename

        ... on MomentUserDTO {
          id
          content
          createdAt
          imgUrls
          share
        }

        ... on CommentDTO {
          id
          comment
          commentDate
          moment {
            id
            content
            images
          }
        }

        ... on HeartDTO {
          id
          moment {
            id
            content
            images
          }
        }
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

let currentFilter = "ALL";

function resetActivityList() {
    activityList.innerHTML = "";
    currentPage = 0;
    hasNext = true;
}

document
    .getElementById("activity-filter-select")
    .addEventListener("change", (e) => {
        currentFilter = e.target.value;
        resetActivityList();
        loadActivities();
    });



/* ================= Helpers ================= */
function renderMomentPreview(images = []) {
    if (!images || images.length === 0) return "";

    const preview = images.slice(0, 3); // chỉ lấy tối đa 3 ảnh

    return `
        <div class="moment-preview">
            ${preview.map(
                url => `<img src="${url}" class="moment-preview-img"  alt=""/>`
            ).join("")}
        </div>
    `;
}
function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString();
}

window.addEventListener("scroll", () => {
    const scrollBottom =
        window.innerHeight + window.scrollY >= document.body.offsetHeight - 300;

    if (scrollBottom) {
        loadActivities();
    }
});

loadActivities();
