package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class UserRequest {
    @NotBlank(message = "username không được để trống")
    private String username;

    @NotBlank(message = "first name không được để trống")
    private String firstName;

    @NotBlank(message = "last name không được để trống")
    private String lastName;

    @Email
    private String email;

    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại phải có 9–11 chữ số")
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
