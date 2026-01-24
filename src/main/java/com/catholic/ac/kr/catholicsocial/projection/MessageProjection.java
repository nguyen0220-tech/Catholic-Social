package com.catholic.ac.kr.catholicsocial.projection;

import java.time.LocalDateTime;

public interface MessageProjection {
    Long getId();
    Long getSenderId();
    String getText();
    LocalDateTime getCreatedAt();
}
