package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.FollowDTO;
import com.catholic.ac.kr.catholicsocial.service.FollowService;
import com.catholic.ac.kr.catholicsocial.status.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    //Đang theo dõi
    @GetMapping()
    public ResponseEntity<ApiResponse<FollowDTO>> getFollowing(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(followService.getAllFollowing(useDetails.getUserId(), page, size));
    }

    //Người theo dõi
    @GetMapping("users")
    public ResponseEntity<ApiResponse<FollowDTO>> getFollowers(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(followService.getAllFollowers(useDetails.getUserId(), page, size));
    }

    @GetMapping("find-blocked")
    public ResponseEntity<ApiResponse<FollowDTO>> getBlockedFollowers(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(followService.getBlockedFollowers(useDetails.getUserId(), page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> follow(
            @AuthenticationPrincipal CustomUserDetails follower,
            @RequestParam Long userId) {
        ApiResponse<String> status = followService.createFollower(follower.getUserId(), userId);
        return ResponseEntity.status(status.getStatus()).body(status);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> userAction(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @RequestParam Long userId,
            @RequestParam Action action) {
        ApiResponse<String> status = followService.userAction(useDetails.getUserId(), userId, action);
        return ResponseEntity.status(status.getStatus()).body(status);
    }

    @PutMapping("block")
    public ResponseEntity<ApiResponse<String>> blockFollow(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @RequestParam Long userId) {
        return ResponseEntity.ok(followService.blockUser(useDetails.getUserId(), userId));
    }
}
