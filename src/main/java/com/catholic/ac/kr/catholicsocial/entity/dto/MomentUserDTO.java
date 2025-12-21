package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/*
thông tin chi tiết moment, mỗi moment có coment + user heart
 */

@Getter @Setter
public class MomentUserDTO {
    private Long id;
    private String content;
    private List<String> imgUrls;
    private LocalDateTime createdAt;
    private String share;

    private List<CommentDTO> comments;
    private List<HeartDTO> hearts;
}
