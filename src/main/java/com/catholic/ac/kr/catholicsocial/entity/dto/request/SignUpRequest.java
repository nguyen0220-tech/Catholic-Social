package com.catholic.ac.kr.catholicsocial.entity.dto.request;

import com.catholic.ac.kr.catholicsocial.status.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class SignUpRequest {
    @NotBlank(message = "username không được để trống")
    private String username;

    @NotBlank(message = "password không được để trống")
    private String password;

    @Pattern(regexp = "^[A-Za-z]{2,10}$", message = "Tên bao gồm chữ cái  phải có 2–10 chữ")
    @NotBlank(message = "first name không được để trống")
    private String firstName;

    @Pattern(regexp = "^[A-Za-z]{2,10}$", message = "Tên bao gồm chữ cái  phải có 2–10 chữ")
    @NotBlank(message = "last không được để trống")
    private String lastName;

    @Email(message = "email không hợp lệ")
    private String email;

    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại phải có 9–11 chữ số")
    private String phone;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull
    private Sex sex;

}
