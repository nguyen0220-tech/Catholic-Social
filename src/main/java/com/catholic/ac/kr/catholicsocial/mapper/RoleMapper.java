package com.catholic.ac.kr.catholicsocial.mapper;

import com.catholic.ac.kr.catholicsocial.entity.dto.RoleDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.Role;

public class RoleMapper {
    public static RoleDTO toRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();

        dto.setName(role.getName());

        return dto;
    }
}
