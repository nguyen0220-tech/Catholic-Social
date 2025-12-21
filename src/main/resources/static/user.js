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

const params = new URLSearchParams(window.location.search);
const paramUserId = params.get("id");
const currentUserId = localStorage.getItem("userId");

const userId = paramUserId ? Number(paramUserId) : Number(currentUserId);


const PROFILE_QUERY = `
query ($userId: ID!) {
  getProfile(userId: $userId) {
    id
    user {
      fullName
      avatarUrl
    }
    moments(page: 0, size: 10) {
      content {
        content
        createdAt
        share
        imgUrls
        comments {
          id
          comment
          commentDate
          user {
            id
            userFullName
            avatarUrl
          }
        }
        hearts {
          heartId
          user {
            id
            userFullName
            avatarUrl
          }
        }
      }
    }
  }
}
`;

loadProfile();

async function loadProfile() {
    const res = await graphqlRequest(PROFILE_QUERY, {userId});
    const profile = res.data.getProfile;

    renderProfile(profile.user);
    renderMoments(profile.moments.content);
}

function renderProfile(user) {
    document.getElementById("profile").innerHTML = `
        <div class="profile-header">
            <img src="${user.avatarUrl}">
            <div>
                <h2>${user.fullName}</h2>
            </div>
        </div>
    `;
}

function renderMoments(moments) {
    const container = document.getElementById("moments");
    container.innerHTML = "";

    moments.forEach(m => {
        container.innerHTML += `
            <div class="moment">
                <p>${m.content}</p>

                ${m.imgUrls.length > 0 ? `
                <div class="moment-images">
                    ${m.imgUrls.map(url => `<img src="${url}">`).join("")}
                </div>` : ""}

                <div class="createdAt">
                    ${new Date(m.createdAt).toLocaleString()} · ${m.share}
                </div>

                <div class="hearts">
                    ❤️ ${m.hearts.length}
                    ${m.hearts.map(h => `
                        <div class="heart-user" onclick="goToProfile(${h.user.id})">
                            <img src="${h.user.avatarUrl}">
                            <span>${h.user.userFullName}</span>
                        </div>
                    `).join("")}
                </div>

                <div class="comments">
                    💬 ${m.comments.length}
                    ${m.comments.map(c => `
                        <div class="comment">
                            <img src="${c.user.avatarUrl}"
                                 onclick="goToProfile(${c.user.id})">
                        
                            <b onclick="goToProfile(${c.user.id})">
                                ${c.user.userFullName}
                            </b>
                        
                            <span>${c.comment}</span>
                        </div>
                    `).join("")}
                </div>
            </div>
        `;
    });
}

function goToProfile(userId) {
    window.location.href = `/user.html?id=${userId}`;
}

window.goToProfile = goToProfile;