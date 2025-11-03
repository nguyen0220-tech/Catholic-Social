package com.catholic.ac.kr.catholicsocial.controller.auth;

import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.TokenResponseDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.LoginRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.LogoutRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.RefreshTokenRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.SignUpRequest;
import com.catholic.ac.kr.catholicsocial.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${app.verification.base-url}")
    private String baseUrl;

    private final AuthService authService;

    @PostMapping("signup")
    public ResponseEntity<ApiResponse<String>> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @GetMapping("verify")
    public ResponseEntity<Boolean> verify(
            @RequestParam String token,
            HttpServletResponse response) throws IOException {

        boolean success = authService.verifyUser(token);

        if (success) {
            response.sendRedirect(baseUrl + "/verify-user.html?success=" + true);
        } else
            response.sendRedirect(baseUrl + "/verify-user.html?fail=" + false);

        return ResponseEntity.ok().build();
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest));
    }

    @PostMapping("logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PostMapping("refresh-token")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
