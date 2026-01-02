package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HeartDTO {
    private Long id;
    private Long userId;
    private Long momentId;
    private UserGQLDTO user;
    private MomentGQLDTO moment;
}
