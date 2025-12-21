package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
public class UserInfoDTO {
    private String fullName;
    private String avatarUrl;
    private LocalDate birthday;
}
