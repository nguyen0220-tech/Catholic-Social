package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class GroupChatRequest {
    private List<Long> memberIds;
    private String roomName;
    private String roomDescription;
}
