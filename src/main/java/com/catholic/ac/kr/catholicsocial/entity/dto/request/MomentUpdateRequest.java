package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import com.catholic.ac.kr.catholicsocial.status.MomentShare;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MomentUpdateRequest {
    private Long momentId;
    private String content;
    private MomentShare share;
}
