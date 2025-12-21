package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
trang cá nhân hiển thị của mỗi user gồm thông tin và các moment
 */
@Getter @Setter
public class UserProfileDTO {
    private Long id;

    private UserInfoDTO user;
    private MomentConnection moments;

}
