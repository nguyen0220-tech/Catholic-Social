package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.FindUsernameRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.UserRequest;
import com.catholic.ac.kr.catholicsocial.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
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
}
