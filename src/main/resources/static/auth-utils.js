// auth-utils.js
const originalFetch = window.fetch;
const API_BASE = window.location.origin;

window.fetch = async (url, options = {}) => {
    let accessToken = localStorage.getItem("accessToken");
    let refreshToken = localStorage.getItem("refreshToken");

    console.log("👉 Gọi API:", url);
    console.log("👉 AccessToken hiện tại:", accessToken ? accessToken.substring(0, 20) + "..." : "null");
    console.log("👉 RefreshToken hiện tại:", refreshToken ? refreshToken.substring(0, 20) + "..." : "null");

    // Thêm Authorization header nếu có accessToken
    options.headers = {
        ...(options.headers || {}),
        ...(accessToken ? { "Authorization": `Bearer ${accessToken}` } : {})
    };

    let response = await originalFetch(url, options);

    // Nếu access token hết hạn (401/403) và không phải request refresh
    if ((response.status === 401 || response.status === 403) && refreshToken && !url.includes("/auth/refresh")) {
        console.warn("⚠️ Access token hết hạn. Thử gọi refresh...");

        try {
            const refreshResponse = await originalFetch(`${API_BASE}/auth/refresh`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refresh_token: refreshToken })
            });

            console.log("👉 Kết quả gọi refresh:", refreshResponse.status);

            if (!refreshResponse.ok) throw new Error("Refresh token thất bại");

            const result = await refreshResponse.json();
            console.log("✅ Refresh thành công. Nhận token mới:", result);

            // Lưu token mới
            localStorage.setItem("accessToken", result.data.accessToken);
            localStorage.setItem("refreshToken", result.data.refreshToken);

            // Retry request gốc với token mới
            options.headers["Authorization"] = `Bearer ${result.data.accessToken}`;
            console.log("🔄 Thử gọi lại request gốc:", url);
            response = await originalFetch(url, options);
        } catch (err) {
            console.error("❌ Refresh token thất bại. Chuyển hướng login...", err);
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            window.location.href = "/auth.html";
        }
    }

    console.log("✅ API trả về status:", response.status);
    return response; // Trả về Response gốc, JS khác dùng .json() như bình thường
};