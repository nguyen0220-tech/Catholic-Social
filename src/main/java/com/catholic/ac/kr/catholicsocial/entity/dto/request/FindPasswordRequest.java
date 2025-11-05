package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FindPasswordRequest {
    @NotBlank(message = "Vui lòng nhập username")
    private String username;

    @Email(message = "email không hợp lệ")
    private String email;

    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại phải có 9–11 chữ số")
    private String phone;
}
