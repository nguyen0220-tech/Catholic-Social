package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageRequest;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.service.MessageService;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("chat")
@RequiredArgsConstructor
public class ChatRoomMessageController {
    private final MessageService messageService;

    @PostMapping("send-direct")
    public ApiResponse<String> sendDirectMessage(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @ModelAttribute MessageRequest request
            ){
        return messageService.sendDirectMessage(useDetails.getUser().getId(), request);
    }
}
