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
    private int numOfMoments; //FK resolver
    private int numOfFollowers; //FK resolver
    private int numOfFollowing; //FK resolver
    private boolean isFollowing; //FK resolver
    private boolean isBlocked; //FK resolver
    private UserInfoDTO user; //FK resolver
    private MomentConnection moments; //FK resolver

}
