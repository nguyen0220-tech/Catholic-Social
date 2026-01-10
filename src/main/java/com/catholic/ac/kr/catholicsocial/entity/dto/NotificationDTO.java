package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Long entityId;
    private String type;
    private boolean isRead;

    private UserGQLDTO actor; //FK resolver
    private Object target; //FK resolver
}
