package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

/*
    thông tin user thực hiện hành động: comment, heart, activity... <chủ yếu dùng GraphQL>
 */

@Getter @Setter
public class UserGQLDTO {
    private Long id;
    private String userFullName;
    private String avatarUrl;
}
