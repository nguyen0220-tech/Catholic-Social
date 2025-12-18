package com.catholic.ac.kr.catholicsocial.projection;

import java.time.LocalDateTime;

public interface CommentProjection {
    Long getId();
    String getComment();
    LocalDateTime getCreatedAt();

    Long getMomentId();
    Long getUserId();

}
