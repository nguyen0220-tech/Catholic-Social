package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class RoomChatDTO {
    private Long chatRoomId;
    private String roomName;
    private  String roomDescription;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
}
