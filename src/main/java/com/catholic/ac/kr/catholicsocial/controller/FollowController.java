package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.entity.dto.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.FollowDTO;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.FollowService;
import com.catholic.ac.kr.catholicsocial.status.ACTION;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FollowDTO>>> getFollowers(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(followService.getAllFollowers(useDetails.getUser().getId(), page, size));
    }

    @GetMapping("find-blocked")
    public ResponseEntity<ApiResponse<List<FollowDTO>>> getBlockedFollowers(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(followService.getBlockedFollowers(useDetails.getUser().getId(), page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> follow(
            @AuthenticationPrincipal CustomUseDetails follower,
            @RequestParam Long userId) {
        ApiResponse<String> status = followService.createFollower(follower.getUser().getId(), userId);
        return ResponseEntity.status(status.getStatus()).body(status);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> userAction(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @RequestParam Long userId,
            @RequestParam ACTION action) {
        ApiResponse<String> status = followService.userAction(useDetails.getUser().getId(), userId, action);
        return ResponseEntity.status(status.getStatus()).body(status);
    }

    @PutMapping("block")
    public ResponseEntity<ApiResponse<String>> blockFollow(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @RequestParam Long userId) {
        return ResponseEntity.ok(followService.blockUser(useDetails.getUser().getId(), userId));
    }
}
