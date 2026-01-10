package com.catholic.ac.kr.catholicsocial.projection;

public interface NotificationProjection {
    Long getId();
    Long getUserId();
    Long getActorId();
    Long getEntityId();
    String getType();
    boolean isRead();
}
