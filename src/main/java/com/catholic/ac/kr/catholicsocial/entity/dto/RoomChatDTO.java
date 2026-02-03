package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class RoomChatDTO {
    private Long chatRoomId;
    private String roomName;
    private  String roomDescription;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
}
