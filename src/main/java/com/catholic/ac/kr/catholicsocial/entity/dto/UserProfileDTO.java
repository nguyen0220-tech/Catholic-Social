package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

/*
trang cá nhân hiển thị của mỗi user gồm thông tin và các moment
 */
@Getter @Setter
public class UserProfileDTO {
    private Long id;
    private boolean isFollowing;
    private boolean isBlocked;
    private UserInfoDTO user;
    private MomentConnection moments;

}
