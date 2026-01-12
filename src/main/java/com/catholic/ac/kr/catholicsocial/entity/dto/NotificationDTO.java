package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Long actorId;
    private Long entityId;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;

    private UserGQLDTO actor; //FK resolver
    private Object target; //FK resolver
}
