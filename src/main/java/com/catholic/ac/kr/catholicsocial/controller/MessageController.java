package com.catholic.ac.kr.catholicsocial.controller;

import com.catholic.ac.kr.catholicsocial.entity.dto.MessageDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageForRoomChatRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.MessageRequest;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetails;
import com.catholic.ac.kr.catholicsocial.service.MessageService;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("chat")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping("send-in-zoom")
    public ApiResponse<MessageDTO> sendMessageInRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute MessageForRoomChatRequest request) {
        return messageService.sendMessageInRoom(userDetails.getUser().getId(), request);
    }

    @PostMapping("send-direct")
    public ApiResponse<MessageDTO> sendDirectMessage(
            @AuthenticationPrincipal CustomUserDetails useDetails,
            @ModelAttribute MessageRequest request
    ) {
        return messageService.sendDirectMessage(useDetails.getUser().getId(), request);
    }
}
