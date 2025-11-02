package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "username không được để trống")
    private String username;

    @NotBlank(message = "password không được để trống")
    private String password;
}
