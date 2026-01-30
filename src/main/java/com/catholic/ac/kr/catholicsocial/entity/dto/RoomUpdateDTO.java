package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/*
    Khi gửi message sẽ bắn Socket đến những member của phòng chat
 */
@AllArgsConstructor
@Getter @Setter
public class RoomUpdateDTO {
    private Long chatRoomId;
    private String lastMessagePreview;

    private LocalDateTime lastMessageAt;
}
