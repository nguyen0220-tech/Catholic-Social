package com.catholic.ac.kr.catholicsocial.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class ProfileDTO {
    private String firstName;
    private String lastName;
    @Email(message = "email không hợp lệ")
    private String email;
    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại phải có 9–11 chữ số")
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String gender;
    private String avatarUrl;
}
