package com.catholic.ac.kr.catholicsocial.entity.dto;

/*
    hiển thị thông tin user khi click list followers, following
 */

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FollowerDTO {
    private Long userId;

    private UserGQLDTO user;
    private boolean isFollowed;
}
