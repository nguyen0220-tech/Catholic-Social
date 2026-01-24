package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateChatRoomRequest {
    private Long chatRoomId;
    private String chatRoomName;
    private String chatRoomDescription;
}
