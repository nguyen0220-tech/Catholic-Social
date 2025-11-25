package com.catholic.ac.kr.catholicsocial.interceptor;

import com.catholic.ac.kr.catholicsocial.security.tokencommon.JwtUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitUtils implements HandlerInterceptor {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;

    private Bucket createBucket() {
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill);

        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String ipAddress = request.getRemoteAddr();
        String path = request.getRequestURI();

        if (path.startsWith("/auth/login") ||
                path.startsWith("/auth/signup") ||
                path.startsWith("/auth/logout") ||
                path.startsWith("/auth/verify") ||
                path.startsWith("user/find-password") ||
                path.startsWith("user/reset-password")) {

            Bucket bucket = buckets.computeIfAbsent(ipAddress, k -> createBucket());

            if (bucket.tryConsume(1)) return true;

            else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too Many Requests");
                return false;
            }
        }

        String token = request.getHeader("Authorization");


        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized");
            return false;
        }

        String pureToken = token.substring(7);
        String username;

        try {
            username = jwtUtil.extractUsername(pureToken);
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token has expired");
            return false;
        }

        /*
        computeIfAbsent() là một phương thức của interface Map trong Java,
        dùng để tự động tính toán và thêm giá trị cho một key nếu key đó chưa có trong map.

        Nếu key đã tồn tại trong map → trả về giá trị hiện có, không gọi hàm mappingFunction.

        Nếu key chưa tồn tại → gọi mappingFunction.apply(key) để tạo ra giá trị mới,
        sau đó put vào map và trả về giá trị đó.
         */

        String rateLimitKey = username + ":" + ipAddress;

        Bucket bucket = buckets.computeIfAbsent(rateLimitKey, k -> createBucket());

        if (bucket.tryConsume(1)) return true;

        else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too Many Requests");
            return false;
        }
    }
}
