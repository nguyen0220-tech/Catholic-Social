package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.MomentDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MomentRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MomentUpdateRequest;
import com.catholic.ac.kr.catholicsocial.service.MomentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("moment")
@RequiredArgsConstructor
public class MomentController {
    private final MomentService momentService;

    @GetMapping("all")
    public ResponseEntity<ApiResponse<List<MomentDTO>>> getAllMoments(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(momentService.getAllMoments(customUserDetails.getUser().getId(), page, size));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MomentDTO>>> getAllMomentsByUserId(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(momentService.getAllMomentsByUserId(useDetails.getUser().getId(), page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> postMoment(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @Valid @ModelAttribute MomentRequest request) {

        ApiResponse<String> response = momentService.uploadMoment(useDetails.getUser().getId(), request);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateMoment(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @RequestBody MomentUpdateRequest request) {
        return ResponseEntity.ok(momentService.updateMoment(useDetails.getUser().getId(), request));
    }

    @DeleteMapping("{momentId}")
    public ResponseEntity<ApiResponse<String>> deleteMoment(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @PathVariable Long momentId) {
        return ResponseEntity.ok(momentService.deleteMoment(useDetails.getUser().getId(), momentId));
    }
}
