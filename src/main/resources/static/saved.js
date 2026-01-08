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

const GET_ALL_SAVED = `
query {
  allSaved(page: 0, size: 5) {
    data {
      id
      userId
      momentId
      heartCount
      moment {
        id
        content
        images
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

const DELETE_SAVED = `
mutation DeleteSaved($momentId: ID!) {
  deleteSaved(momentId: $momentId) {
    success
    message
  }
}
`;

async function loadSaved() {
    const result = await graphqlRequest(GET_ALL_SAVED);
    const savedList = result.data.allSaved.data;

    const container = document.getElementById("saved-list");
    container.innerHTML = "";

    if (!savedList.length) {
        container.innerHTML = "<p>Chưa có moment nào được lưu.</p>";
        return;
    }

    savedList.forEach(saved => {
        const div = document.createElement("div");
        div.className = "moment";
        div.style.cursor = "pointer";

        // 👉 click vào card → sang detail
        div.onclick = () => {
            window.location.href = `moment-detail.html?id=${saved.momentId}`;
        };

        div.innerHTML = `
            <div class="images">
                ${saved.moment.images.map(img => `<img src="${img}" />`).join("")}
            </div>

            <div class="moment-content">
                <h4>${saved.moment.content}</h4>

                <div class="moment-footer">
                    <span class="heart">❤️ ${saved.heartCount}</span>
                    <button class="btn-remove">Bỏ lưu</button>
                </div>
            </div>
        `;

        // Chặn click bubble khi bấm "Bỏ lưu"
        div.querySelector(".btn-remove").onclick = (e) => {
            e.stopPropagation();
            toggleDeleteSaved(saved.momentId);
        };

        container.appendChild(div);
    });
}

async function toggleDeleteSaved(momentId) {
    const res = await graphqlRequest(DELETE_SAVED, { momentId });

    if (res.errors) {
        console.error("GraphQL errors:", res.errors);
        alert(res.errors[0].message);
        return;
    }

    const result = res.data.deleteSaved;

    if (!result.success) {
        alert(result.message);
        return;
    }

    await loadSaved();
}

loadSaved();

