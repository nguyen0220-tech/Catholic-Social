// --- SWITCH FORMS ---
const loginForm = document.getElementById("loginForm");
const signUpForm = document.getElementById("signUpForm");

document.getElementById("showSignUp").addEventListener("click", () => {
    loginForm.style.display = "none";
    signUpForm.style.display = "block";
});

document.getElementById("showLogin").addEventListener("click", () => {
    signUpForm.style.display = "none";
    loginForm.style.display = "block";
});

// --- LOGIN ---
document.getElementById("loginBtn").addEventListener("click", async function () {
    const username = document.getElementById("loginUsername").value.trim();
    const password = document.getElementById("loginPassword").value.trim();
    const loginError = document.getElementById("loginError");

    loginError.textContent = "";

    if (!username || !password) {
        loginError.textContent = "Vui lòng nhập username và password.";
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        const result = await response.json();

        if (result.success) {
            localStorage.setItem("accessToken", result.data.accessToken);
            localStorage.setItem("refreshToken", result.data.refreshToken);
            localStorage.setItem("userId", result.data.userId);
            window.location.href = "home.html";
        } else {
            loginError.textContent = result.message || "Đăng nhập thất bại.";
        }

    } catch (err) {
        console.error("Login error:", err);
        loginError.textContent = "Lỗi kết nối đến server.";
    }
});

// --- SIGN UP ---
document.getElementById("signUpBtn").addEventListener("click", async function () {
    const requestBody = {
        username: document.getElementById("signUpUsername").value.trim(),
        password: document.getElementById("signUpPassword").value.trim(),
        firstName: document.getElementById("firstName").value.trim(),
        lastName: document.getElementById("lastName").value.trim(),
        email: document.getElementById("email").value.trim(),
        phone: document.getElementById("phone").value.trim(),
        birthDate: document.getElementById("birthDate").value,
        sex: document.getElementById("sex").value
    };

    const fields = ["username","password","firstName","lastName","email","phone","birthDate","sex"];

    // 1. Xóa các lỗi cũ
    fields.forEach(f => {
        const span = document.getElementById(f + "Error");
        if (span) span.textContent = "";
    });

    try {
        const response = await fetch("http://localhost:8080/auth/signup", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestBody)
        });

        const result = await response.json();

        if (result.success) {
            alert(result.message || "Sign up thành công! Vui lòng kiểm tra email để xác thực.");
            signUpForm.style.display = "none";
            loginForm.style.display = "block";
        } else {
            if (result.data) {
                // Hiển thị lỗi từng field
                for (const field in result.data) {
                    const span = document.getElementById(field + "Error");
                    if (span) span.textContent = result.data[field];
                }
            } else {
                // Nếu backend trả lỗi chung
                const signUpError = document.getElementById("signUpError");
                signUpError.textContent = result.message || "Sign up thất bại.";
            }
        }

    } catch (err) {
        console.error("Sign up error:", err);
        const signUpError = document.getElementById("signUpError");
        signUpError.textContent = "Lỗi kết nối đến server.";
    }
});

// --- SWITCH FORMS ---
const findUsernameForm = document.getElementById("findUsernameForm");

// Hiển thị Find Username form
document.getElementById("showFindUsername")?.addEventListener("click", () => {
    loginForm.style.display = "none";
    signUpForm.style.display = "none";
    findUsernameForm.style.display = "block";
});

// Quay lại Login từ Find Username
document.getElementById("showLoginFromFind")?.addEventListener("click", () => {
    findUsernameForm.style.display = "none";
    loginForm.style.display = "block";
});

// --- FIND USERNAME ---
document.getElementById("findUsernameBtn").addEventListener("click", async function () {
    const requestBody = {
        firstName: document.getElementById("findFirstName").value.trim(),
        lastName: document.getElementById("findLastName").value.trim(),
        phone: document.getElementById("findPhone").value.trim()
    };

    // Xóa lỗi cũ
    ["findFirstName","findLastName","findPhone"].forEach(f => {
        const span = document.getElementById(f + "Error");
        if (span) span.textContent = "";
    });
    document.getElementById("findUsernameError").textContent = "";

    // Client-side validation
    for (const key in requestBody) {
        if (!requestBody[key]) {
            const span = document.getElementById("find" + key.charAt(0).toUpperCase() + key.slice(1) + "Error");
            if (span) span.textContent = `Vui lòng nhập ${key}`;
            return;
        }
    }

    try {
        const response = await fetch("http://localhost:8080/user/find-username", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestBody)
        });

        const result = await response.json();

        if (result.success) {
            alert(result.message || `Tài khoản của bạn: ${result.data}`);
            findUsernameForm.style.display = "none";
            loginForm.style.display = "block";
        } else {
            // Hiển thị lỗi từng field nếu có
            if (result.data) {
                for (const field in result.data) {
                    const span = document.getElementById("find" + field.charAt(0).toUpperCase() + field.slice(1) + "Error");
                    if (span) span.textContent = result.data[field];
                }
            } else {
                document.getElementById("findUsernameError").textContent = result.message || "Không tìm thấy username.";
            }
        }
    } catch (err) {
        console.error("Find Username error:", err);
        document.getElementById("findUsernameError").textContent = "Lỗi kết nối đến server.";
    }
});

