package com.catholic.ac.kr.catholicsocial.projection;

import java.time.LocalDateTime;

public interface NotificationProjection {
    Long getId();
    Long getUserId();
    Long getActorId();
    Long getEntityId();
    String getType();
    boolean getIsRead();
    LocalDateTime getCreatedAt();
}
