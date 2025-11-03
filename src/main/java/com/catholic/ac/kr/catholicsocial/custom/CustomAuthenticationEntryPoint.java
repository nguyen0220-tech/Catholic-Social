package com.catholic.ac.kr.catholicsocial.custom;

import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
Khi Spring Security chặn request vì chưa đăng nhập hoặc token sai,
nó không ném AuthenticationException trực tiếp tới Controller.

Thay vào đó, Spring Security sẽ xử lý bằng AuthenticationEntryPoint
(mặc định trả về JSON hoặc HTML) → nên handler @ExceptionHandler(AuthenticationException.class) không được gọi.
 */

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException{

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> exception =  ApiResponse.exception(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Unauthorized: "+authException.getMessage(),
                request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(exception));
    }

}
