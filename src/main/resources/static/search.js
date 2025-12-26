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
        body: JSON.stringify({ query, variables })
    });
    return await res.json();
}

let page = 0;
let size = 5;
let hasNext = true;

const SEARCH_MUTATION = `
mutation Search($request: SearchRequest!) {
  search(request: $request) {
    message
  }
}
`;

const DELETE_SEARCH_MUTATION = `
mutation DeleteSearch($searchId: ID) {
  deleteSearch(searchId: $searchId) {
    message
  }
}
`;

const SEARCH_HISTORY_QUERY = `
query SearchHistory($page: Int!, $size: Int!) {
  searchHistory(page: $page, size: $size) {
    data {
      id
      keyword
      createdAt
    }
    pageInfo {
      page
      size
      hasNext
    }
  }
}
`;

const input = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");
const historyList = document.getElementById("historyList");
const loadMoreBtn = document.getElementById("loadMoreBtn");

loadMoreBtn.addEventListener("click", () => {
    if (hasNext) {
        loadSearchHistory();
    }
});

async function loadSearchHistory() {

    if (!hasNext) return;

    const res = await graphqlRequest(SEARCH_HISTORY_QUERY, { page, size });
    const result = res.data.searchHistory;

    result.data.forEach(renderItem);

    hasNext = result.pageInfo.hasNext;
    page++;

    loadMoreBtn.style.display = hasNext ? "block" : "none";
}


function renderItem(item) {
    const li = document.createElement("li");

    const keywordSpan = document.createElement("span");
    keywordSpan.textContent = item.keyword;
    keywordSpan.style.cursor = "pointer";

    keywordSpan.addEventListener("click", () => {
        doSearch(item.keyword, false);
    });

    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "❌";

    deleteBtn.addEventListener("click", async (e) => {
        e.stopPropagation(); // 🚫 không trigger search
        await graphqlRequest(DELETE_SEARCH_MUTATION, {
            searchId: item.id
        });
        li.remove();
    });

    li.appendChild(keywordSpan);
    li.appendChild(deleteBtn);
    historyList.appendChild(li);
}

const userResultList = document.getElementById("userResultList");

async function searchUsers(keyword, page, size) {
    const res = await fetch(
        `${URL_BASE}/user/find-follow?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`,
        {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        }
    );
    return await res.json();
}

function renderUser(user) {
    const li = document.createElement("li");

    li.innerHTML = `
        <a href="user.html?id=${user.id}"
           style="display:flex; align-items:center; gap:8px;
                  text-decoration:none; color:#333;">
            <img src="${user.userAvatarUrl || '/icon/default-avatar.png'}"
                 width="32" height="32"
                 style="border-radius:50%" />
            <span style="font-weight:600">
                ${user.firstName} ${user.lastName}
            </span>
        </a>
    `;

    userResultList.appendChild(li);
}

searchBtn.addEventListener("click", async () => {
    const keyword = input.value.trim();
    await doSearch(keyword, true);
});

async function doSearch(keyword, saveHistory = false) {
    if (!keyword) return;

    input.value = keyword;

    userResultList.innerHTML = "";

    const res = await searchUsers(keyword, 0, 10);

    const users = res?.data ?? [];
    users.forEach(renderUser);

    if (saveHistory) {
        await graphqlRequest(SEARCH_MUTATION, {
            request: { keyword }
        });

        page = 0;
        hasNext = true;
        historyList.innerHTML = "";
    }
}

const suggestionList = document.getElementById("suggestionList");

async function loadUserSuggestions() {
    const res = await fetch(`${URL_BASE}/user/suggestion`, {
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });

    const json = await res.json();
    const users = json?.data ?? [];

    suggestionList.innerHTML = "";

    users.forEach(renderSuggestionUser);
}

function renderSuggestionUser(user) {
    const li = document.createElement("li");

    const container = document.createElement("div");
    container.style.display = "flex";
    container.style.alignItems = "center";
    container.style.gap = "10px";

    container.innerHTML = `
        <img src="${user.userAvatarUrl || '/icon/default-avatar.png'}"
             width="36" height="36"
             style="border-radius:50%" />

        <div style="flex:1">
            <a href="user.html?id=${user.id}"
               style="text-decoration:none; font-weight:600; color:#333">
                ${user.firstName} ${user.lastName}
            </a>
            <div style="font-size:12px; color:#777">
                Theo dõi bởi ${user.fistNameUserSuggestions}
                ${user.lastNameUserSuggestions}
            </div>
        </div>
    `;

    const followBtn = document.createElement("button");
    followBtn.className = "follow-btn";
    followBtn.textContent = "Theo dõi";
    followBtn.style.borderRadius="20%";
    followBtn.style.background = "rgb(238,130,238)";
    followBtn.style.color = "#00fb00";

    followBtn.addEventListener("click", async () => {
        followBtn.disabled = true;
        followBtn.textContent = "Đang theo dõi...";

        try {
            const res = await followUser(user.id);

            if (res.success) {
                followBtn.textContent = "Đã theo dõi";
                followBtn.style.background = "#efefef";
                followBtn.style.color = "#777";

                // ✨ optional: xoá user khỏi list suggestion
                setTimeout(() => li.remove(), 500);
            } else {
                throw new Error(res.message);
            }
        } catch (e) {
            followBtn.disabled = false;
            followBtn.textContent = "Theo dõi";
            alert("Follow thất bại");
        }
    });

    container.appendChild(followBtn);
    li.appendChild(container);
    suggestionList.appendChild(li);
}

async function followUser(userId) {
    const res = await fetch(`${URL_BASE}/follow?userId=${userId}`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${accessToken}`
        }
    });

    return await res.json();
}

await loadUserSuggestions()
await loadSearchHistory();

