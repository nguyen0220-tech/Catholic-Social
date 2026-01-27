package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class UserForAddRoomChatDTO {
    private Long userId;
    private Boolean inRoom;

    public UserForAddRoomChatDTO(Long userId) {
        this.userId = userId;
    }
}
