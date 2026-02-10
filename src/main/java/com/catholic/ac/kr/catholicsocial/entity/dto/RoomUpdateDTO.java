package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
    Khi gửi message sẽ bắn Socket đến những member của phòng chat
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class RoomUpdateDTO {
    private Long chatRoomId;
    private String lastMessagePreview;
    private String roomName;

    private LocalDateTime lastMessageAt;
}
