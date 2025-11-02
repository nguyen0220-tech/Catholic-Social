package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FindUsernameRequest {
    @NotBlank(message = "Vui lòng điền first name")
    private String firstName;

    @NotBlank(message = "Vui lòng điền last name")
    private String lastName;

    @NotBlank(message = "Vui lòng điền số điện thoại")
    private String phone;
}
