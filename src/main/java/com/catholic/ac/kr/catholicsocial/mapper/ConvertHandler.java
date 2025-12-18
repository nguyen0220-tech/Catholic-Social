package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.UserGQLDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.User;

public class ConvertHandler {

    public static UserGQLDTO convertToUserGQLDTO(User user) {
        UserGQLDTO userGQLDTO = new UserGQLDTO();

        userGQLDTO.setId(user.getId());
        userGQLDTO.setUserFullName(user.getUserInfo().getFirstName()+" "+user.getUserInfo().getLastName());
        userGQLDTO.setAvatarUrl(user.getUserInfo().getAvatarUrl());

        return userGQLDTO;
    }
}
