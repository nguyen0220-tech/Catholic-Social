package com.catholic.ac.kr.catholicsocial.entity.dto;

import com.catholic.ac.kr.catholicsocial.wrapper.MomentConnection;
import lombok.Getter;
import lombok.Setter;

/*
trang cá nhân hiển thị của mỗi user gồm thông tin và các moment
 */
@Getter @Setter
public class UserProfileDTO {
    private Long id;
    private boolean isFollowing; //FK resolver
    private boolean isBlocked; //FK resolver
    private UserInfoDTO user; //FK resolver
    private MomentConnection moments; //FK resolver

}
