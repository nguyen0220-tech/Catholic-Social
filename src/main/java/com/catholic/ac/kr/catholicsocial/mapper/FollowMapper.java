package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.FollowDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Follow;

import java.util.ArrayList;
import java.util.List;

public class FollowMapper {
    public static FollowDTO toFollowDTO(Follow follow) {
        FollowDTO followDTO = new FollowDTO();

        followDTO.setUserAvatarUrl(follow.getUser().getUserInfo().getAvatarUrl());
        followDTO.setUserId(follow.getUser().getId());
        followDTO.setUserName(follow.getUser().getUserInfo().getFirstName() + " " + follow.getUser().getUserInfo().getLastName());

        return followDTO;
    }

    public static List<FollowDTO> toFollowDTO(List<Follow> follows) {
        List<FollowDTO> followDTOs = new ArrayList<>();

        for (Follow follow : follows) {
            followDTOs.add(toFollowDTO(follow));
        }

        return followDTOs;
    }
}
