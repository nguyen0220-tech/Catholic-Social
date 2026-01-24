package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ChatRoomDetailDTO {
    private String roomName;
    private String description;
    List<UserGQLDTO> members;
}
