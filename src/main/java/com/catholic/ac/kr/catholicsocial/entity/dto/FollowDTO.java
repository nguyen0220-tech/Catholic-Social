package com.catholic.ac.kr.catholicsocial.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class FollowDTO {
    private int userNums;
    private List<UserGQLDTO> users;
}
