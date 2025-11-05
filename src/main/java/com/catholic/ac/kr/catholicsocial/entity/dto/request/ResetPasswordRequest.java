package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResetPasswordRequest {
    @NotBlank(message = "")
    private String token;

    @NotBlank(message = "Vui lòng nhập mập khẩu mới")
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    private String confirmPassword;
}
