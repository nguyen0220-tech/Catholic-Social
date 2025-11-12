package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class MomentDTO {
    private Long id;
    private String userFullName;
    private String userAvatar;
    private String content;
    private List<String> imageUrls;
    private boolean edited;
    private LocalDateTime createdAt;
    private String share;
}
