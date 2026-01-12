package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.NotificationDTO;
import com.catholic.ac.kr.catholicsocial.projection.NotificationProjection;

import java.util.List;

public class NotificationMapper {
    public static NotificationDTO toNotificationDTO(NotificationProjection projection) {
        NotificationDTO dto = new NotificationDTO();

        dto.setId(projection.getId());
        dto.setUserId(projection.getUserId());
        dto.setActorId(projection.getActorId());
        dto.setEntityId(projection.getEntityId() != null ? projection.getEntityId() : null);
        dto.setRead(projection.getIsRead());
        dto.setType(projection.getType());
        dto.setCreatedAt(projection.getCreatedAt());

        return dto;
    }

    public static List<NotificationDTO> toNotificationDTO(List<NotificationProjection> projections) {
        return projections.stream()
                .map(NotificationMapper::toNotificationDTO)
                .toList();
    }
}
