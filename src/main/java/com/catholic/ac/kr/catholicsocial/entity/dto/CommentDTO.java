package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class CommentDTO {
    private Long id;
    private Long momentId;
    private UserGQLDTO user;
    private String comment;
    private LocalDateTime commentDate;
}
