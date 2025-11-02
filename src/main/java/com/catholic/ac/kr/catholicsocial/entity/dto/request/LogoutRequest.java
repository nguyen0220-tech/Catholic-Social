package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LogoutRequest {
    @NotBlank
    private String token;
}
