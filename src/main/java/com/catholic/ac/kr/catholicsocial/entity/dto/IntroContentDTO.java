package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class IntroContentDTO {
    private String url;
    private LocalDateTime exp;
    private String publicId;
}
