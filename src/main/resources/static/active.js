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
          }
        }

        ... on HeartDTO {
          id
          moment {
            id
            content
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

    const res = await graphqlRequest(QUERY_GET_ACTIVE, {
        page: currentPage,
        size: PAGE_SIZE
    });

    if (res.errors) {
        console.error("GraphQL errors:", res.errors);
        loadingEl.textContent = "Không thể tải activity";
        return;
    }

    const { data, pageInfo } = res.data.allActive;

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

    const { userFullName, avatarUrl } = active.user;
    const userId = active.user.id;

    const div = document.createElement("div");
    div.className = "activity-item";

    const actorHtml = `
        <a href="user.html?id=${userId}" class="activity-actor">
            <img class="activity-avatar" src="${avatarUrl || 'icon/default-avatar.png'}" />
            <span class="activity-name">${userFullName}</span>
        </a>
    `;

    switch (target.__typename) {

        case "MomentUserDTO":
            div.innerHTML = `
                <div class="activity-header">
                    ${actorHtml}
                    <br>
                    <span class="activity-text">📤 đã đăng bài viết mới</span>
                </div>
                <div class="activity-time">${formatDate(target.createdAt)}</div>

                <div class="activity-content">
                    <p>${target.content}</p>
                    ${renderImages(target.imgUrls)}
                </div>
            `;
            break;

        case "CommentDTO":
            div.innerHTML = `
                <div class="activity-header">
                    ${actorHtml}
                    <br>
                    <span class="activity-text">🗯️ đã bình luận</span>
                </div>
                <div class="activity-time">${formatDate(target.commentDate)}</div>

                <div class="activity-content">
                    <p>"${target.comment}"</p>
                    <small>Bài viết: ${target.moment.content}</small>
                </div>
            `;
            break;

        case "HeartDTO":
            div.innerHTML = `
                <div class="activity-header">
                    ${actorHtml}
                    <br>
                    <span class="activity-text">❤️ đã thích bài viết</span>
                </div>

                <div class="activity-content">
                    <p>${target.moment.content}</p>
                </div>
            `;
            break;
    }

    activityList.appendChild(div);
}


/* ================= Helpers ================= */
function getActorInfo(active) {
    if (active.user) {
        return {
            name: active.user.userFullName,
            avatar: active.user.avatarUrl
        };
    }

    return {
        name: "Hệ thống",
        avatar: "icon/default-avatar.png"
    };
}

function renderImages(imgUrls = []) {
    return imgUrls
        .map(url => `<img src="${url}"  alt=""/>`)
        .join("");
}

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString();
}

/* ================= Infinite Scroll ================= */

window.addEventListener("scroll", () => {
    const scrollBottom =
        window.innerHeight + window.scrollY >= document.body.offsetHeight - 300;

    if (scrollBottom) {
        loadActivities();
    }
});

/* Load lần đầu */
loadActivities();
