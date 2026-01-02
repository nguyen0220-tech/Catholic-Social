package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ActiveDTO {
    private Long id;
    private Long entityId;
    private String type;
    private Long userId;
    private UserGQLDTO user;
    private Object target;
}
