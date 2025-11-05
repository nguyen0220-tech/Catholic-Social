package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.ProfileDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.User;

import java.util.stream.Collectors;

//ADMIN quản lí
public class UserMapper {
    public static UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setUserId(user.getId());
        userDTO.setFirstName(user.getUserInfo().getFirstName());
        userDTO.setLastName(user.getUserInfo().getLastName());
        userDTO.setEmail(user.getUserInfo().getEmail());
        userDTO.setPhoneNumber(user.getUserInfo().getPhone());
        userDTO.setEnabled(user.isEnabled());
        userDTO.setSex(user.getUserInfo().getSex().toString());
        userDTO.setBirthDate(user.getUserInfo().getBirthday());

        userDTO.setRoles(
                user.getRoles()
                        .stream()
                        .map(RoleMapper::toRoleDTO)
                        .collect(Collectors.toSet())
        );

        return userDTO;
    }

    //thông tin user
    public static ProfileDTO toProfileDTO(User user) {
        ProfileDTO profileDTO = new ProfileDTO();

        profileDTO.setFirstName(user.getUserInfo().getFirstName());
        profileDTO.setLastName(user.getUserInfo().getLastName());
        profileDTO.setEmail(user.getUserInfo().getEmail());
        profileDTO.setPhone(user.getUserInfo().getPhone());
        profileDTO.setAddress(user.getUserInfo().getAddress());
        profileDTO.setBirthDate(user.getUserInfo().getBirthday());
        profileDTO.setGender(String.valueOf(user.getUserInfo().getSex()));
        profileDTO.setAvatarUrl(user.getUserInfo().getAvatarUrl());

        return profileDTO;
    }

}
