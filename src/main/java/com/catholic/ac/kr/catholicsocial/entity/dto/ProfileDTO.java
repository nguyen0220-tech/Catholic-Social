package com.catholic.ac.kr.catholicsocial.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class ProfileDTO {
    @Pattern(regexp = "^[A-Za-z]{2,10}$", message = "Tên bao gồm chữ cái  phải có 2–10 chữ")
    private String firstName;
    @Pattern(regexp = "^[A-Za-z]{2,10}$", message = "Tên bao gồm chữ cái  phải có 2–10 chữ")
    private String lastName;
    @Size(max = 255, message = "Tiểu sử tối đa 255 ký tự")
    private String bio;
    @Email(message = "email không hợp lệ")
    private String email;
    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại phải có 9–11 chữ số")
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String gender;
    private String avatarUrl;
}
