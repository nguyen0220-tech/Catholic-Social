package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TokenResponseDTO {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}
