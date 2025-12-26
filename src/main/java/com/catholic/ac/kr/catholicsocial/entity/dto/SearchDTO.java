package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class SearchDTO {
    private Long id;
    private String keyword;
    private LocalDateTime createdAt;
}
