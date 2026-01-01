package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter @Setter
public class FollowDTO {
    private int userNums;
    private Set<Long> followingUserIdSet; //dùng để lưu userId của những user mà currentUser đang following trong số những user đang following currentUser
    private List<UserGQLDTO> users;
}
