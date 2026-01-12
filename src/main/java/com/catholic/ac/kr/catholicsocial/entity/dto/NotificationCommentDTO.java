package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;
 /*
    chứa thông báo của comment
  */
@Getter @Setter
public class NotificationCommentDTO {
    private String momentComment;
    private Long momentId;

    private MomentGQLDTO moment;
}
