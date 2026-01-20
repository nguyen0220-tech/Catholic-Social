package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.entity.dto.IntroVideoDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.IntroVideoRequest;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.service.IntroVideoService;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("intro")
@RequiredArgsConstructor
public class IntroVideoController {
    private final IntroVideoService introVideoService;

    @PostMapping
    public ResponseEntity<ApiResponse<IntroVideoDTO>> uploadIntro(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @ModelAttribute IntroVideoRequest introFile) {
        return ResponseEntity.ok(introVideoService.uploadIntro(useDetails.getUser().getId(), introFile));
    }

    @PutMapping("/remove/{introId}")
    public ResponseEntity<ApiResponse<String>> removeIntro(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @PathVariable Long introId) {
        return ResponseEntity.ok(introVideoService.removeIntro(useDetails.getUser().getId(), introId));
    }

    @PutMapping("/restore/{introId}")
    public ResponseEntity<ApiResponse<String>> restoreIntro(
            @AuthenticationPrincipal CustomUseDetails useDetails,
            @PathVariable Long introId) {
        return ResponseEntity.ok(introVideoService.restoreIntro(useDetails.getUser().getId(), introId));
    }

}
