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
          user {
            userFullName
            avatarUrl
          }
          moment {
            id
            content
          }
        }

        ... on HeartDTO {
          id
          user {
            userFullName
            avatarUrl
          }
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

let currentPage = 1;
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
    if (!target) return;

    const { name, avatar } = getActorInfo(target);

    const div = document.createElement("div");
    div.className = "activity-item";

    switch (target.__typename) {

        case "MomentUserDTO":
            div.innerHTML = `
                <div class="activity-header">
                    <img class="activity-avatar" src="${avatar}" />
                    <div>
                        <div class="activity-title">📸 ${name} đã đăng bài viết mới</div>
                        <div class="activity-time">${formatDate(target.createdAt)}</div>
                    </div>
                </div>

                <div class="activity-content">
                    <p>${target.content}</p>
                    ${renderImages(target.imgUrls)}
                </div>
            `;
            break;

        case "CommentDTO":
            div.innerHTML = `
                <div class="activity-header">
                    <img class="activity-avatar" src="${avatar}" />
                    <div>
                        <div class="activity-title">💬 ${name} đã bình luận</div>
                        <div class="activity-time">${formatDate(target.commentDate)}</div>
                    </div>
                </div>

                <div class="activity-content">
                    <p>"${target.comment}"</p>
                    <small>Bài viết: ${target.moment.content}</small>
                </div>
            `;
            break;

        case "HeartDTO":
            div.innerHTML = `
                <div class="activity-header">
                    <img class="activity-avatar" src="${avatar}" />
                    <div>
                        <div class="activity-title">❤️ ${name} đã thích bài viết</div>
                    </div>
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
function getActorInfo(target) {
    // Comment / Heart → có user
    if (target.user) {
        return {
            name: target.user.userFullName,
            avatar: target.user.avatarUrl
        };
    }

    // MomentUserDTO → chính mình
    return {
        name: "Bạn",
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
