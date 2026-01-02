package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.entity.dto.*;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.FindPasswordRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.FindUsernameRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.ResetPasswordRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UserRequest;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.UserService;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    @Value("${app.verification.base-url}")
    private String baseUrl;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createdUser(request));
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @PostMapping("find-username")
    public ResponseEntity<ApiResponse<String>> findUsername(@Valid @RequestBody FindUsernameRequest request) {
        return ResponseEntity.ok(userService.findUsernameForgot(request));
    }

    @PostMapping("find-password")
    public ResponseEntity<ApiResponse<String>> findPassword(@Valid @RequestBody FindPasswordRequest request) {
        return ResponseEntity.ok(userService.forgotPassword(request));
    }


    @PostMapping("reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    @GetMapping("verify-reset-password")
    public void verifyTokenAndRedirect(@RequestParam String token, HttpServletResponse response) throws IOException {
        // Redirect sang FE reset-password.html kèm token
        response.sendRedirect(baseUrl + "/auth-reset-password.html?token=" + token);
    }

    @GetMapping("profile")
    public ResponseEntity<ApiResponse<ProfileDTO>> getUserProfile(@AuthenticationPrincipal CustomUseDetails useDetails) {
        return ResponseEntity.ok(userService.getUserProfile(useDetails.getUser().getId()));
    }

    @PutMapping("update-profile")
    public ResponseEntity<ApiResponse<String>> updateUserProfile(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @Valid @RequestBody ProfileDTO profileDTO) {
        return ResponseEntity.ok(userService.updateProfile(useDetails.getUser().getId(), profileDTO));
    }

    @PostMapping("upload-avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @RequestParam("file") MultipartFile file){
        return ResponseEntity.ok(userService.uploadAvatar(useDetails.getUser().getId(), file));
    }

    @GetMapping("find-follow")
    public ResponseEntity<ApiResponse<List<UserFollowDTO>>> getUserFollow(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @RequestParam String keyword,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(userService.findUserFollow(useDetails.getUser().getId(),keyword, page, size));
    }

    @GetMapping("suggestion")
    public ResponseEntity<ApiResponse<List<UserSuggestions>>> getUserSuggestions(@AuthenticationPrincipal CustomUseDetails useDetails) {
        return ResponseEntity.ok(userService.findUserSuggestions(useDetails.getUser().getId()));
    }
}
