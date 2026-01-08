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
    private List<String> imgUrls; //FK resolver
    private LocalDateTime createdAt;
    private String share;

    private boolean saved; //FK resolver
    private List<CommentDTO> comments; //FK resolver
    private List<HeartDTO> hearts; //FK resolver

}
