package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

/*
    chứa thông báo của follow

 */
@Getter @Setter
public class NotificationFollowerDTO {
    private Long followerId;

    private boolean reverseFollowed; //FK resolver
}
