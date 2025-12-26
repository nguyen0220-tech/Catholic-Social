package com.catholic.ac.kr.catholicsocial.projection;

import java.time.LocalDateTime;

public interface SearchProjection {
    Long getId();
    String getKeyword();
    LocalDateTime getCreatedAt();
}
