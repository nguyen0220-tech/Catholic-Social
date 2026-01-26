package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddMemberRequest {
    private Long memberId;
    private Long chatRoomId;
}
