package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.FollowDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Follow;

import java.util.ArrayList;
import java.util.List;

public class FollowMapper {


    //Map đang theo dõi
    public static FollowDTO mapFollowingUses(List<Follow> follows) {
        FollowDTO followDTO = new FollowDTO();

        List<UserGQLDTO> userGQLDTOList = new ArrayList<>();

        for (Follow follow : follows) {
            UserGQLDTO userGQLDTO = new UserGQLDTO();
            userGQLDTO.setId(follow.getUser().getId());
            userGQLDTO.setUserFullName(follow.getUser().getUserInfo().getFirstName() + " " + follow.getUser().getUserInfo().getLastName());
            userGQLDTO.setAvatarUrl(follow.getUser().getUserInfo().getAvatarUrl());

            userGQLDTOList.add(userGQLDTO);
        }

        followDTO.setUsers(userGQLDTOList);

        return followDTO;
    }

    //Map người theo dõi
    public static FollowDTO mapUsersFollowing(List<Follow> follows) {
        FollowDTO followDTO = new FollowDTO();

        List<UserGQLDTO> userGQLDTOList = new ArrayList<>();

        for (Follow follow : follows) {
            UserGQLDTO userGQLDTO = new UserGQLDTO();
            userGQLDTO.setId(follow.getFollower().getId());
            userGQLDTO.setUserFullName(follow.getFollower().getUserInfo().getFirstName() + " " + follow.getFollower().getUserInfo().getLastName());
            userGQLDTO.setAvatarUrl(follow.getFollower().getUserInfo().getAvatarUrl());

            userGQLDTOList.add(userGQLDTO);
        }

        followDTO.setUsers(userGQLDTOList);

        return followDTO;
    }
}
